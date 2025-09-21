package fun.icystal.chat.advisor;

import fun.icystal.chat.mapper.MessageMapper;
import fun.icystal.core.entity.MessageLog;
import fun.icystal.core.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static fun.icystal.core.context.UserHolder.getConversationId;

/**
 * 使用 mysql 记录消息
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecordAdvisor implements BaseAdvisor {

    private final MessageMapper messageMapper;

    @NotNull
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, @NotNull AdvisorChain advisorChain) {

        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        recordMessage(userMessage);
        return chatClientRequest;
    }

    @NotNull
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, @NotNull AdvisorChain advisorChain) {
        if (chatClientResponse.chatResponse() != null) {
            AssistantMessage message = chatClientResponse.chatResponse().getResult().getOutput();
            recordMessage(message);
        }
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2000;
    }

    private void recordMessage(Message message) {
        if (message == null) {
            return;
        }
        String conversationId = getConversationId();
        MessageLog messageLog = new MessageLog(conversationId, message.getText(), message.getMessageType().getValue(), LocalDateTime.now());
        int insert = messageMapper.insert(messageLog);
        log.info("conversation {} insert {} message {} ", conversationId, insert, JsonUtil.toJSONString(messageLog));
    }
}
