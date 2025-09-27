package fun.icystal.chat.advisor;

import fun.icystal.chat.mapper.MessageMapper;
import fun.icystal.chat.service.SummaryService;
import fun.icystal.chat.entity.MessageLog;
import fun.icystal.chat.util.JsonUtil;
import fun.icystal.chat.util.SnowFlake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static fun.icystal.chat.context.UserHolder.getConversationId;

/**
 * 使用 mysql 记录消息
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecordAdvisor implements BaseAdvisor {

    private final MessageMapper messageMapper;

    private final SummaryService summaryService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 最小短期记忆轮次
     */
    @Value("${memory.term.short.min}")
    private int minShortTerm;

    /**
     * 最大短期对话数量
     */
    @Value("${memory.term.short.max}")
    private int maxShortTerm;

    @NotNull
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, @NotNull AdvisorChain advisorChain) {

        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        recordMessage(userMessage, 0);
        return chatClientRequest;
    }

    @NotNull
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, @NotNull AdvisorChain advisorChain) {
        if (chatClientResponse.chatResponse() != null) {
            AssistantMessage message = chatClientResponse.chatResponse().getResult().getOutput();
            executor.execute(() -> recordMessage(message, 1));
        }
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2000;
    }

    /**
     * 记录
     * @param message 需要记录的消息
     * @param mode 0 不开启摘要  1 开启摘要
     */
    private void recordMessage(Message message, int mode) {
        if (message == null) {
            return;
        }
        String conversationId = getConversationId();
        MessageLog messageLog = new MessageLog(SnowFlake.id(), conversationId, message.getText(), message.getMessageType().getValue(), LocalDateTime.now(), null);
        int insert = messageMapper.insert(messageLog);
        log.info("conversation {} insert {} message {} ", conversationId, insert, JsonUtil.toJSONString(messageLog));
        if (mode == 0) {
            return;
        }

        List<MessageLog> messageLogs = messageMapper.selectByConversationId(conversationId, 100);
        messageLogs.sort(Comparator.comparing(MessageLog::time));

        List<MessageLog> unSummaryMessages = messageLogs.stream().filter(m -> m.summaryId() == null).toList();
        if (unSummaryMessages.size() >= maxShortTerm) {
            summaryService.summary(unSummaryMessages.subList(0, unSummaryMessages.size() - minShortTerm));
        }
    }
}
