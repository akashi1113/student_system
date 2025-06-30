package com.csu.sms.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 课程表现DTO
 */
public class CoursePerformanceDTO {
    private Long courseId;
    private String courseTitle;
    private BigDecimal averageScore;
    private Integer examCount;
    private Long studyDuration; // 学习时长(秒)
    private Integer videoCount;
    private Integer completedVideos;
    private BigDecimal completionRate;

    // 构造函数
    public CoursePerformanceDTO() {}

    public CoursePerformanceDTO(Long courseId, String courseTitle, BigDecimal averageScore,
                                Integer examCount, Long studyDuration, Integer videoCount,
                                Integer completedVideos) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.averageScore = averageScore;
        this.examCount = examCount;
        this.studyDuration = studyDuration;
        this.videoCount = videoCount;
        this.completedVideos = completedVideos;

        // 计算完成率 (使用Java 17的语法优化)
        if (videoCount != null && videoCount > 0) {
            this.completionRate = new BigDecimal(completedVideos)
                    .divide(new BigDecimal(videoCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
    }

    // Getters and Setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public BigDecimal getAverageScore() { return averageScore; }
    public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }

    public Integer getExamCount() { return examCount; }
    public void setExamCount(Integer examCount) { this.examCount = examCount; }

    public Long getStudyDuration() { return studyDuration; }
    public void setStudyDuration(Long studyDuration) { this.studyDuration = studyDuration; }

    public Integer getVideoCount() { return videoCount; }
    public void setVideoCount(Integer videoCount) { this.videoCount = videoCount; }

    public Integer getCompletedVideos() { return completedVideos; }
    public void setCompletedVideos(Integer completedVideos) { this.completedVideos = completedVideos; }

    public BigDecimal getCompletionRate() { return completionRate; }
    public void setCompletionRate(BigDecimal completionRate) { this.completionRate = completionRate; }

    // 获取格式化的学习时长
    public String getFormattedStudyDuration() {
        if (studyDuration == null) return "--";
        long hours = studyDuration / 3600;
        long minutes = (studyDuration % 3600) / 60;
        long seconds = studyDuration % 60;
        if (hours > 0) {
            return String.format("%d时%d分%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}