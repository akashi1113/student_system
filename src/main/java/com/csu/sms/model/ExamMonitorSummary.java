package com.csu.sms.model;

import java.util.Date;

public class ExamMonitorSummary {
    private Long id;
    private Long examId;
    private Long userId;
    private Integer totalCount;
    private Integer abnormalCount;
    private Integer noFaceCount;
    private Integer multipleFacesCount;
    private Integer lookingAwayCount;
    private Integer unknownPersonCount;
    private String riskLevel;
    private Date createTime;
    private Date updateTime;

    // 构造方法
    public ExamMonitorSummary() {}

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

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getAbnormalCount() {
        return abnormalCount;
    }

    public void setAbnormalCount(Integer abnormalCount) {
        this.abnormalCount = abnormalCount;
    }

    public Integer getNoFaceCount() {
        return noFaceCount;
    }

    public void setNoFaceCount(Integer noFaceCount) {
        this.noFaceCount = noFaceCount;
    }

    public Integer getMultipleFacesCount() {
        return multipleFacesCount;
    }

    public void setMultipleFacesCount(Integer multipleFacesCount) {
        this.multipleFacesCount = multipleFacesCount;
    }

    public Integer getLookingAwayCount() {
        return lookingAwayCount;
    }

    public void setLookingAwayCount(Integer lookingAwayCount) {
        this.lookingAwayCount = lookingAwayCount;
    }

    public Integer getUnknownPersonCount() {
        return unknownPersonCount;
    }

    public void setUnknownPersonCount(Integer unknownPersonCount) {
        this.unknownPersonCount = unknownPersonCount;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
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