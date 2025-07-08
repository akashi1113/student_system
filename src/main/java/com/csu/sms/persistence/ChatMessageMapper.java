package com.csu.sms.persistence;
import com.csu.sms.model.chat.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    void insertMessage(ChatMessage message);

    List<ChatMessage> findBySessionId(@Param("sessionId") String sessionId);

    List<ChatMessage> findBySessionIdWithLimit(@Param("sessionId") String sessionId,
                                               @Param("limit") int limit);

    void deleteBySessionId(@Param("sessionId") String sessionId);

    int countBySessionId(@Param("sessionId") String sessionId);

    List<ChatMessage> findLatestMessages(@Param("sessionId") String sessionId,
                                         @Param("limit") int limit);
}