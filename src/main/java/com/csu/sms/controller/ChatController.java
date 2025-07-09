package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;

import com.csu.sms.dto.chat.ChatHistoryResponse;
import com.csu.sms.dto.chat.ChatRequest;
import com.csu.sms.dto.chat.ChatResponse;
import com.csu.sms.model.chat.ChatSession;
import com.csu.sms.service.ChatService;
import com.csu.sms.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatService chatService;

    /**
     * 创建新的聊天会话
     */
    @PostMapping("/session")
    public ApiResponse<ChatSession> createSession(@RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        logger.info("创建新会话，用户ID: {}", userId);
        return chatService.createSession(userId);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public ApiResponse<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request, @RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        request.setUserId(userId);
        logger.info("发送消息，会话ID: {}, 消息长度: {}",
                request.getSessionId(), request.getMessage().length());
        return chatService.sendMessage(request);
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history/{sessionId}")
    public ApiResponse<ChatHistoryResponse> getChatHistory(@PathVariable String sessionId) {
        logger.info("获取聊天历史，会话ID: {}", sessionId);
        return chatService.getChatHistory(sessionId);
    }

    /**
     * 获取用户的所有会话
     */
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSession>> getUserSessions(@RequestHeader("Authorization") String token) {
        Long userId=jwtUtil.extractUserId(token);
        logger.info("获取用户会话列表，用户ID: {}", userId);
        return chatService.getUserSessions(userId);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId) {
        logger.info("删除会话，会话ID: {}", sessionId);
        return chatService.deleteSession(sessionId);
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/session/{sessionId}/title")
    public ApiResponse<Void> updateSessionTitle(@PathVariable String sessionId,
                                                @RequestParam String title) {
        logger.info("更新会话标题，会话ID: {}, 新标题: {}", sessionId, title);
        return chatService.updateSessionTitle(sessionId, title);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("聊天服务运行正常", "OK");
    }
}