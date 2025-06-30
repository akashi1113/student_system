package com.csu.sms.model.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ExamNotification {
    private Long id;
    private Long bookingId;
    private Long userId;
    private String notificationType; // BOOKING_CONFIRMED, BOOKING_CANCELLED, EXAM_REMINDER, EXAM_CHANGED, SYSTEM_NOTICE

    private String title;
    private String content;
    private String sendMethod; // SYSTEM, EMAIL, SMS, PUSH

    private String sendStatus; // PENDING, SENT, FAILED, READ
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;

    private String priority; // LOW, NORMAL, HIGH, URGENT

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public ExamNotification() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSendMethod() { return sendMethod; }
    public void setSendMethod(String sendMethod) { this.sendMethod = sendMethod; }

    public String getSendStatus() { return sendStatus; }
    public void setSendStatus(String sendStatus) { this.sendStatus = sendStatus; }

    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }

    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}