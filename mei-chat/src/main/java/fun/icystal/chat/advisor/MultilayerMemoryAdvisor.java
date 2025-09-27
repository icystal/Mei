package fun.icystal.chat.advisor;

import fun.icystal.chat.mapper.MessageMapper;
import fun.icystal.chat.mapper.SummaryMapper;
import fun.icystal.chat.prompt.SystemPromptService;
import fun.icystal.chat.util.BeanMapper;
import fun.icystal.chat.entity.MessageLog;
import fun.icystal.chat.entity.Summary;
import fun.icystal.chat.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 多层记忆的实现
 * 传递 m 轮完整的对话 作为 短期记忆
 * 传递 m~n 轮对话的摘要 作为 摘要记忆
 * 从历史对话的数据库中查询 k 条历史 作为 长期记忆
 */
@Component
@Slf4j
public final class MultilayerMemoryAdvisor implements BaseChatMemoryAdvisor {

    private final SummaryMapper summaryMapper;

    /**
     * 最大短期对话数量
     */
    @Value("${memory.term.short.max}")
    private int maxShortTerm;

    /**
     * 长期记忆条数
     */
    @Value("${memory.term.long}")
    private int longTerm = 5;

    private final MessageMapper messageMapper;

    private final SystemPromptService systemPromptService;


    private MultilayerMemoryAdvisor(MessageMapper messageMapper, SystemPromptService systemPromptService, SummaryMapper summaryMapper) {
        this.messageMapper = messageMapper;
        this.systemPromptService = systemPromptService;
        this.summaryMapper = summaryMapper;
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
                .collect(Collectors.toList());

        List<UserMessage> userMessages = instructions.stream()
                .filter(message -> MessageType.USER.equals(message.getMessageType()))
                .map(message -> (UserMessage) message)
                .filter(message -> StringUtils.isNotBlank(message.getText()))
                .toList();

        List<Message> toolMessages = instructions.stream()
                .filter(message -> MessageType.TOOL.equals(message.getMessageType()))
                .toList();

        SystemMessage systemMessage = systemPromptService.prompt();
        systemMessages.add(systemMessage);

        // 查询历史消息, 并过滤出还没有生成摘要的消息. 对于超出限制的消息, 生成摘要, 并移除队列
        List<MessageLog> messageLogs = messageMapper.selectByConversationId(conversationId, maxShortTerm);
        log.debug("查询到历史消息: {}", JsonUtil.toJSONString(messageLogs));
        if (CollectionUtils.isEmpty(messageLogs)) {
            messageLogs = List.of();
        }

        messageLogs.sort(Comparator.comparing(MessageLog::time));

        Set<Long> summaryIds = new HashSet<>();
        for (MessageLog messageLog : messageLogs) {
            if (messageLog.summaryId() == null || !summaryIds.add(messageLog.summaryId())) {
                continue;
            }

            Summary summary = summaryMapper.selectBySummaryId(messageLog.summaryId());
            if (summary != null && StringUtils.isNotBlank(summary.content())) {
                String messagePrompt = "[对话摘要] " + summary.content();
                systemMessages.add(new SystemMessage(messagePrompt));
            }
        }


        List<Message> shortMemory = messageLogs.stream()
                .filter(messageLog -> Objects.isNull(messageLog.summaryId()))
                .map(BeanMapper::convertMessage)
                .filter(Objects::nonNull)
                .toList();

        List<Message> messages = new ArrayList<>();
        messages.addAll(systemMessages);
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
