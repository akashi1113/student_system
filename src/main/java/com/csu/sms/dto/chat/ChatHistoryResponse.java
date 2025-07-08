package com.csu.sms.dto.chat;

import com.csu.sms.model.chat.ChatMessage;

import java.util.List;

public class ChatHistoryResponse {
    private String sessionId;
    private String title;
    private List<ChatMessage> messages;

    // 构造函数
    public ChatHistoryResponse() {}

    public ChatHistoryResponse(String sessionId, String title, List<ChatMessage> messages) {
        this.sessionId = sessionId;
        this.title = title;
        this.messages = messages;
    }

    // getter和setter
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}
