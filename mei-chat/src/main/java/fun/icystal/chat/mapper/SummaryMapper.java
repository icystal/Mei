package fun.icystal.chat.mapper;

import fun.icystal.chat.entity.Summary;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SummaryMapper {

    @Insert("INSERT INTO summary(summary_id, conversation_id, content, time) VALUES(#{summaryId}, #{conversationId}, #{content}, #{time})")
    void insert(Summary record);

    @Select("SELECT * FROM summary WHERE summary_id = #{summaryId}")
    Summary selectBySummaryId(@Param("summaryId") Long summaryId);
}
