package com.csu.sms.model.experiment;

import java.time.LocalDateTime;

public class ExperimentBooking {
    private Long id;
    private Long experimentId;
    private String experimentName;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status; // 0-待进行, 1-进行中, 2-已完成, 3-已取消
    private Integer approvalStatus; // 0-待审批, 1-已通过, 2-已拒绝
    private Long timeSlotId; // 新增时间段ID
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExperimentId() {
        return experimentId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public void setExperimentId(Long experimentId) {
        this.experimentId = experimentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ExperimentBooking{" +
                "id=" + id +
                ", experimentId=" + experimentId +
                ", userId=" + userId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", approvalStatus=" + approvalStatus +
                ", timeSlotId=" + timeSlotId +
                ", createdAt=" + createdAt +
                '}';
    }


}
