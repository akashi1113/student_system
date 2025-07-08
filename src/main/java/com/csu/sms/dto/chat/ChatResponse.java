package com.csu.sms.dto.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ChatResponse {
    private String content;
    private String messageType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // 构造函数
    public ChatResponse() {}

    public ChatResponse(String content) {
        this.content = content;
        this.messageType = "ASSISTANT";
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String content, String messageType) {
        this.content = content;
        this.messageType = messageType;
        this.timestamp = LocalDateTime.now();
    }

    // getter和setter
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

