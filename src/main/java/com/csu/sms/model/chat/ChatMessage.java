package com.csu.sms.model.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ChatMessage {
    private Long id;
    private String sessionId;
    private String messageType; // USER, ASSISTANT
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // 构造函数
    public ChatMessage() {}

    public ChatMessage(String sessionId, String messageType, String content) {
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // getter和setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}