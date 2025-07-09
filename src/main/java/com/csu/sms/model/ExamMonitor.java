package com.csu.sms.model;

import java.math.BigDecimal;
import java.util.Date;

public class ExamMonitor {
    private Long id;
    private Long examId;
    private Long userId;
    private String imagePath;
    private Long timestamp;
    private String monitorResult;
    private Integer abnormalCount;
    private String abnormalType;
    private BigDecimal faceScore;
    private BigDecimal yawAngle;
    private BigDecimal pitchAngle;
    private BigDecimal rollAngle;
    private Integer status;
    private Date createTime;
    private Date updateTime;

    // 构造方法
    public ExamMonitor() {}

    // getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMonitorResult() {
        return monitorResult;
    }

    public void setMonitorResult(String monitorResult) {
        this.monitorResult = monitorResult;
    }

    public Integer getAbnormalCount() {
        return abnormalCount;
    }

    public void setAbnormalCount(Integer abnormalCount) {
        this.abnormalCount = abnormalCount;
    }

    public String getAbnormalType() {
        return abnormalType;
    }

    public void setAbnormalType(String abnormalType) {
        this.abnormalType = abnormalType;
    }

    public BigDecimal getFaceScore() {
        return faceScore;
    }

    public void setFaceScore(BigDecimal faceScore) {
        this.faceScore = faceScore;
    }

    public BigDecimal getYawAngle() {
        return yawAngle;
    }

    public void setYawAngle(BigDecimal yawAngle) {
        this.yawAngle = yawAngle;
    }

    public BigDecimal getPitchAngle() {
        return pitchAngle;
    }

    public void setPitchAngle(BigDecimal pitchAngle) {
        this.pitchAngle = pitchAngle;
    }

    public BigDecimal getRollAngle() {
        return rollAngle;
    }

    public void setRollAngle(BigDecimal rollAngle) {
        this.rollAngle = rollAngle;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}