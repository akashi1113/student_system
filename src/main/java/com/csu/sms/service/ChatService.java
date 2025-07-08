package com.csu.sms.service;

import com.csu.sms.common.ApiResponse;

import com.csu.sms.dto.chat.ChatHistoryResponse;
import com.csu.sms.dto.chat.ChatRequest;
import com.csu.sms.dto.chat.ChatResponse;
import com.csu.sms.model.chat.ChatMessage;
import com.csu.sms.model.chat.ChatSession;
import com.csu.sms.persistence.ChatMessageMapper;
import com.csu.sms.persistence.ChatSessionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatSessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper messageMapper;

    @Autowired
    private ChatAIService chatAIService;

    /**
     * 创建新的聊天会话
     */
    public ApiResponse<ChatSession> createSession(Long userId) {
        try {
            String sessionId = UUID.randomUUID().toString();
            ChatSession session = new ChatSession(sessionId, userId, "新对话");

            sessionMapper.insertSession(session);
            logger.info("创建新会话成功: {}", sessionId);

            return ApiResponse.success("会话创建成功", session);

        } catch (Exception e) {
            logger.error("创建会话失败", e);
            return ApiResponse.error("创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息并获取AI回复
     */
    public ApiResponse<ChatResponse> sendMessage(ChatRequest request) {
        try {
            // 验证会话是否存在
            ChatSession session = sessionMapper.findBySessionId(request.getSessionId());
            if (session == null) {
                return ApiResponse.error("会话不存在");
            }

            // 保存用户消息
            ChatMessage userMessage = new ChatMessage(
                    request.getSessionId(),
                    "USER",
                    request.getMessage()
            );
            messageMapper.insertMessage(userMessage);

            // 获取历史对话
            List<ChatMessage> history = messageMapper.findBySessionId(request.getSessionId());

            // 生成AI回复
            String aiResponse = chatAIService.generateResponse(request.getMessage(), history);

            // 保存AI回复
            ChatMessage aiMessage = new ChatMessage(
                    request.getSessionId(),
                    "ASSISTANT",
                    aiResponse
            );
            messageMapper.insertMessage(aiMessage);

            // 如果是第一条消息，生成会话标题
            if (messageMapper.countBySessionId(request.getSessionId()) == 2) {
                String title = chatAIService.generateTitle(request.getMessage());
                sessionMapper.updateSessionTitle(request.getSessionId(), title);
            }

            // 更新会话时间戳
            sessionMapper.updateSessionTimestamp(request.getSessionId());

            ChatResponse response = new ChatResponse(aiResponse);
            return ApiResponse.success("消息发送成功", response);

        } catch (Exception e) {
            logger.error("发送消息失败", e);
            return ApiResponse.error("发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 获取聊天历史记录
     */
    public ApiResponse<ChatHistoryResponse> getChatHistory(String sessionId) {
        try {
            ChatSession session = sessionMapper.findBySessionId(sessionId);
            if (session == null) {
                return ApiResponse.error("会话不存在");
            }

            List<ChatMessage> messages = messageMapper.findBySessionId(sessionId);

            ChatHistoryResponse response = new ChatHistoryResponse(
                    sessionId,
                    session.getTitle(),
                    messages
            );

            return ApiResponse.success("获取历史记录成功", response);

        } catch (Exception e) {
            logger.error("获取历史记录失败", e);
            return ApiResponse.error("获取历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有会话
     */
    public ApiResponse<List<ChatSession>> getUserSessions(Long userId) {
        try {
            List<ChatSession> sessions = sessionMapper.findByUserId(userId);
            return ApiResponse.success("获取会话列表成功", sessions);

        } catch (Exception e) {
            logger.error("获取会话列表失败", e);
            return ApiResponse.error("获取会话列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除会话
     */
    public ApiResponse<Void> deleteSession(String sessionId) {
        try {
            // 删除会话消息
            messageMapper.deleteBySessionId(sessionId);

            // 删除会话
            sessionMapper.deleteSession(sessionId);

            logger.info("删除会话成功: {}", sessionId);
            return ApiResponse.success("会话删除成功", null);

        } catch (Exception e) {
            logger.error("删除会话失败", e);
            return ApiResponse.error("删除会话失败: " + e.getMessage());
        }
    }

    /**
     * 更新会话标题
     */
    public ApiResponse<Void> updateSessionTitle(String sessionId, String title) {
        try {
            sessionMapper.updateSessionTitle(sessionId, title);
            return ApiResponse.success("标题更新成功", null);

        } catch (Exception e) {
            logger.error("更新会话标题失败", e);
            return ApiResponse.error("更新会话标题失败: " + e.getMessage());
        }
    }
}