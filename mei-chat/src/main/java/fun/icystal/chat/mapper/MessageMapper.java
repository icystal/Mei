package fun.icystal.chat.mapper;

import fun.icystal.core.entity.MessageLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MessageMapper {

    @Select("SELECT DISTINCT conversation_id FROM message")
    List<String> selectAllConversationIds();

    @Delete("DELETE FROM message WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") String conversationId);

    @Insert("INSERT INTO message(message_id, conversation_id, content, type, time) VALUES(#{messageId}, #{conversationId}, #{content}, #{type}, #{time})")
    int insert(MessageLog message);

    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} ORDER BY time DESC LIMIT #{limit}")
    List<MessageLog> selectByConversationId(String conversationId, int limit);

    @Update("UPDATE message SET summary_id = #{summaryId} WHERE message_id = #{messageId}")
    int updateSummaryId(Long messageId, Long summaryId);
}
