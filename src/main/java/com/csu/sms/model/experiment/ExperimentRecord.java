package com.csu.sms.model.experiment;

import java.time.LocalDateTime;

public class ExperimentRecord {
    private Long id;
    private Long bookingId;
    private String stepData; // JSON格式存储实验步骤
    private String parameters; // JSON格式存储参数设置
    private String resultData; // JSON格式存储实验结果
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getStepData() {
        return stepData;
    }

    public void setStepData(String stepData) {
        this.stepData = stepData;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ExperimentRecord{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", stepData='" + stepData + '\'' +
                ", parameters='" + parameters + '\'' +
                ", resultData='" + resultData + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createdAt=" + createdAt +
                '}';
    }
}
