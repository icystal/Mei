package fun.icystal.core.entity;

import java.time.LocalDateTime;

public record MessageLog(
        String conversationId,
        String content,
        String type,
        LocalDateTime time
) {}
