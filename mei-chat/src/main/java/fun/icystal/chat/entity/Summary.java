package fun.icystal.chat.entity;

import java.time.LocalDateTime;

public record Summary(Long summaryId,
                      String conversationId,
                      String content,
                      LocalDateTime time) {
}
