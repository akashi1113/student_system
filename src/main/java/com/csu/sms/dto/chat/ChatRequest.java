package com.csu.sms.dto.chat;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private Long userId;

    // 构造函数
    public ChatRequest() {}

    public ChatRequest(String sessionId, String message, Long userId) {
        this.sessionId = sessionId;
        this.message = message;
        this.userId = userId;
    }

    // getter和setter
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}