package fun.icystal.chat.service;

import fun.icystal.chat.mapper.MessageMapper;
import fun.icystal.chat.mapper.SummaryMapper;
import fun.icystal.core.context.UserHolder;
import fun.icystal.core.dto.MessageSummaryDTO;
import fun.icystal.core.entity.MessageLog;
import fun.icystal.core.entity.Summary;
import fun.icystal.core.util.JsonUtil;
import fun.icystal.core.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SummaryService implements InitializingBean {

    private final MessageMapper messageMapper;

    private final SummaryMapper summaryMapper;

    private final ChatClient chatClient;

    @Value("${memory.summary.prompt}")
    private String summaryPrompt;

    public SummaryService(MessageMapper messageMapper, SummaryMapper summaryMapper, ChatClient.Builder builder) {
        this.messageMapper = messageMapper;
        this.summaryMapper = summaryMapper;
        this.chatClient = builder.build();
    }

    @Transactional
    public void summary(List<MessageLog> messages) {
        MessageSummaryDTO messageSummaryDTO = chatClient.prompt()
                .system(summaryPrompt)
                .user(buildMessageText(messages))
                .call()
                .entity(MessageSummaryDTO.class);
        log.info("对话摘要结果: {}", JsonUtil.toJSONString(messageSummaryDTO));

        if (messageSummaryDTO == null || StringUtils.isBlank(messageSummaryDTO.getSummary())) {
            return;
        }

        long summaryId = SnowFlake.id();
        Summary summary = new Summary(summaryId, UserHolder.getConversationId(), messageSummaryDTO.getSummary(), LocalDateTime.now());
        summaryMapper.insert(summary);

        for (MessageLog message : messages) {
            messageMapper.updateSummaryId(message.messageId(), summaryId);
        }
    }

    private String buildMessageText(List<MessageLog> messages) {
        StringBuilder sb = new StringBuilder();
        for (MessageLog message : messages) {
            if (message == null || StringUtils.isBlank(message.content())) {
                continue;
            }
            String content = message.content().replace('\n', ' ');
            if (MessageType.USER.getValue().equals(message.type())) {
                sb.append("对方: ").append(content).append("\n");
            } else if (MessageType.ASSISTANT.getValue().equals(message.type())) {
                sb.append("我: ").append(content).append("\n");
            }
        }
        return sb.toString();
    }


    @Override
    public void afterPropertiesSet() {
        log.info("生成摘要的系统提示词: {}", summaryPrompt);
    }
}
