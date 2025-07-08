package com.csu.sms.persistence;

import com.csu.sms.model.chat.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatSessionMapper {

    void insertSession(ChatSession session);

    ChatSession findBySessionId(@Param("sessionId") String sessionId);

    List<ChatSession> findByUserId(@Param("userId") Long userId);

    void updateSession(ChatSession session);

    void deleteSession(@Param("sessionId") String sessionId);

    void updateSessionTitle(@Param("sessionId") String sessionId,
                            @Param("title") String title);

    void updateSessionTimestamp(@Param("sessionId") String sessionId);
}