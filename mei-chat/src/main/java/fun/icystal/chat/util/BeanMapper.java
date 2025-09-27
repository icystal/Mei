package fun.icystal.chat.util;

import fun.icystal.chat.entity.MessageLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.*;

import java.util.List;

public class BeanMapper {

    public static Message convertMessage(MessageLog messageLog) {
        if (messageLog == null || StringUtils.isBlank(messageLog.content()) || StringUtils.isBlank(messageLog.type())) {
            return null;
        }

        return switch (messageLog.type()) {
            case "user" -> new UserMessage(messageLog.content());
            case "assistant" -> new AssistantMessage(messageLog.content());
            case "system" -> new SystemMessage(messageLog.content());
            // The content is always stored empty for ToolResponseMessages.
            // If we want to capture the actual content, we need to extend
            // AddBatchPreparedStatement to support it.
            case "tool" -> new ToolResponseMessage(List.of());
            default -> throw new IllegalStateException("Unexpected value: " + messageLog.type());
        };
    }
}
