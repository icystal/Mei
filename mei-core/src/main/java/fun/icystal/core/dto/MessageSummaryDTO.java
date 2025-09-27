package fun.icystal.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSummaryDTO {

    /**
     * 对话的摘要信息
     */
    private String summary;

    /**
     * 这段对话的话题标签列表(尽量不为空), 如: ["环保", "战争"]
     */
    private List<String> tags;

    /**
     * 被测人的性格标签列表(可以为空), 如: ["幽默", "高傲", "散漫"]
     */
    private List<String> personalityTags;

    /**
     * 被测人的思想标签(可以为空), 如: ["自由主义", "小资情调"]
     */
    private List<String> ideologicalTag;

}
