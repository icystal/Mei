package fun.icystal.chat.advisor;

import fun.icystal.chat.mapper.MessageMapper;
import fun.icystal.chat.prompt.SystemPromptService;
import fun.icystal.chat.util.BeanMapper;
import fun.icystal.core.entity.MessageLog;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 多层记忆的实现
 * 传递 m 轮完整的对话 作为 短期记忆
 * 传递 m~n 轮对话的摘要 作为 摘要记忆
 * 从历史对话的数据库中查询 k 条历史 作为 长期记忆
 */
@Component
public final class MultilayerMemoryAdvisor implements BaseChatMemoryAdvisor {

    /**
     * 短期记忆轮次
     */
    private final int shortTerm = 10;

    /**
     * 摘要范围
     */
    private final int summaryScope = 10;

    /**
     * 长期记忆条数
     */
    private final int longTerm = 5;

    private final MessageMapper messageMapper;

    private final SystemPromptService systemPromptService;

    private MultilayerMemoryAdvisor(MessageMapper messageMapper, SystemPromptService systemPromptService) {

        this.messageMapper = messageMapper;
        this.systemPromptService = systemPromptService;
    }

    @NotNull
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, @NotNull AdvisorChain advisorChain) {

        String conversationId = getConversationId(chatClientRequest.context(), "cannot-use-this");

        List<Message> instructions = chatClientRequest.prompt().getInstructions();

        List<SystemMessage> systemMessages = instructions.stream()
                .filter(message -> MessageType.SYSTEM.equals(message.getMessageType()))
                .map(message -> (SystemMessage) message)
                .filter(message -> StringUtils.isNotBlank(message.getText()))
                .toList();

        List<UserMessage> userMessages = instructions.stream()
                .filter(message -> MessageType.USER.equals(message.getMessageType()))
                .map(message -> (UserMessage) message)
                .filter(message -> StringUtils.isNotBlank(message.getText()))
                .toList();

        List<Message> toolMessages = instructions.stream()
                .filter(message -> MessageType.TOOL.equals(message.getMessageType()))
                .toList();

        StringBuilder systemPrompt = new StringBuilder();
        if (!CollectionUtils.isEmpty(systemMessages)) {
            for (SystemMessage systemMessage : systemMessages) {
                systemPrompt.append(systemMessage.getText()).append("\n");
            }
        }

        systemPrompt.append(systemPromptService.prompt());

        SystemMessage systemMessage = new SystemMessage(systemPrompt.toString());

        List<MessageLog> messageLogs = messageMapper.selectByConversationId(conversationId, shortTerm);
        if (CollectionUtils.isEmpty(messageLogs)) {
            messageLogs = List.of();
        }

        List<Message> shortMemory = messageLogs.stream()
                .map(BeanMapper::convertMessage)
                .filter(Objects::nonNull)
                .toList();

        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.addAll(shortMemory);
        messages.addAll(userMessages);
        messages.addAll(toolMessages);

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().mutate().messages(messages).build())
                .build();
    }

    @NotNull
    @Override
    public ChatClientResponse after(@NotNull ChatClientResponse chatClientResponse, @NotNull AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Advisor.DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER;
    }
}
