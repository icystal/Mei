package fun.icystal.core.entity;

import java.time.LocalDateTime;

public record MessageLog(
        Long messageId,
        String conversationId,
        String content,
        String type,
        LocalDateTime time,
        Long summaryId
) {}
