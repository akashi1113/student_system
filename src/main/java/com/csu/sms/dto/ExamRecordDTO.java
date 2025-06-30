package com.csu.sms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试记录DTO
 */
public class ExamRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long examId;
    private String examTitle;
    private Integer score;
    private Integer maxScore;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer duration; // 实际用时(秒)
    private String status;
    private String statusText;
    private Integer violationCount; // 违规次数
    private Integer attemptNumber; // 第几次尝试
    private Boolean isPassed; // 是否通过
    private Integer rankPosition; // 排名位置

    // 考试信息
    private Integer totalScore;
    private Integer passingScore;
    private String examType;
    private String examDescription;
    private Integer examDuration; // 考试时长(分钟)

    // 构造函数
    public ExamRecordDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        // 设置状态文本
        if ("NOT_STARTED".equals(status)) {
            this.statusText = "未开始";
        } else if ("IN_PROGRESS".equals(status)) {
            this.statusText = "进行中";
        } else if ("SUBMITTED".equals(status)) {
            this.statusText = "已提交";
        } else if ("TIMEOUT".equals(status)) {
            this.statusText = "超时";
        } else if ("CANCELLED".equals(status)) {
            this.statusText = "已取消";
        } else {
            this.statusText = "未知";
        }
    }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public Integer getViolationCount() { return violationCount; }
    public void setViolationCount(Integer violationCount) { this.violationCount = violationCount; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public Boolean getIsPassed() { return isPassed; }
    public void setIsPassed(Boolean isPassed) { this.isPassed = isPassed; }

    public Integer getRankPosition() { return rankPosition; }
    public void setRankPosition(Integer rankPosition) { this.rankPosition = rankPosition; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public String getExamDescription() { return examDescription; }
    public void setExamDescription(String examDescription) { this.examDescription = examDescription; }

    public Integer getExamDuration() { return examDuration; }
    public void setExamDuration(Integer examDuration) { this.examDuration = examDuration; }

    // 计算是否及格
    public boolean isPassed() {
        if (score == null || passingScore == null) return false;
        return score >= passingScore;
    }

    // 计算百分比
    public BigDecimal getPercentage() {
        if (score == null || maxScore == null || maxScore == 0) return BigDecimal.ZERO;
        return new BigDecimal(score).divide(new BigDecimal(maxScore), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));
    }

    // 格式化用时
    public String getFormattedDuration() {
        if (duration == null) return "--";
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        if (hours > 0) {
            return String.format("%d时%d分%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}