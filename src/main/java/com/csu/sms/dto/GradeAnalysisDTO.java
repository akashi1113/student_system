package com.csu.sms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩分析DTO
 */
public class GradeAnalysisDTO {
    private Long userId;
    private String username;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 成绩统计
    private BigDecimal averageScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private Integer totalExams;
    private Integer passedExams;
    private BigDecimal passRate;

    // 学习统计
    private Long totalStudyDuration; // 总学习时长(秒)
    private Integer totalStudyDays;  // 学习天数
    private Long averageDailyDuration; // 平均每日学习时长
    private Integer totalVideos; // 总观看视频数
    private Integer completedVideos; // 完成视频数
    private BigDecimal completionRate; // 完成率

    // 综合评价
    private String performanceLevel; // 优秀、良好、一般、较差
    private BigDecimal efficiencyScore; // 学习效率分数
    private List<String> suggestions; // 学习建议

    // 课程详情
    private List<com.csu.sms.dto.CoursePerformanceDTO> coursePerformances;

    // 构造函数
    public GradeAnalysisDTO() {}

    // Getters and Setters (使用Java 17的简化语法)
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }



    public BigDecimal getAverageScore() { return averageScore; }
    public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }

    public BigDecimal getMaxScore() { return maxScore; }
    public void setMaxScore(BigDecimal maxScore) { this.maxScore = maxScore; }

    public BigDecimal getMinScore() { return minScore; }
    public void setMinScore(BigDecimal minScore) { this.minScore = minScore; }

    public Integer getTotalExams() { return totalExams; }
    public void setTotalExams(Integer totalExams) { this.totalExams = totalExams; }

    public Integer getPassedExams() { return passedExams; }
    public void setPassedExams(Integer passedExams) { this.passedExams = passedExams; }

    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }

    public Long getTotalStudyDuration() { return totalStudyDuration; }
    public void setTotalStudyDuration(Long totalStudyDuration) { this.totalStudyDuration = totalStudyDuration; }

    public Integer getTotalStudyDays() { return totalStudyDays; }
    public void setTotalStudyDays(Integer totalStudyDays) { this.totalStudyDays = totalStudyDays; }

    public Long getAverageDailyDuration() { return averageDailyDuration; }
    public void setAverageDailyDuration(Long averageDailyDuration) { this.averageDailyDuration = averageDailyDuration; }

    public Integer getTotalVideos() { return totalVideos; }
    public void setTotalVideos(Integer totalVideos) { this.totalVideos = totalVideos; }

    public Integer getCompletedVideos() { return completedVideos; }
    public void setCompletedVideos(Integer completedVideos) { this.completedVideos = completedVideos; }

    public BigDecimal getCompletionRate() { return completionRate; }
    public void setCompletionRate(BigDecimal completionRate) { this.completionRate = completionRate; }

    public String getPerformanceLevel() { return performanceLevel; }
    public void setPerformanceLevel(String performanceLevel) { this.performanceLevel = performanceLevel; }

    public BigDecimal getEfficiencyScore() { return efficiencyScore; }
    public void setEfficiencyScore(BigDecimal efficiencyScore) { this.efficiencyScore = efficiencyScore; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public List<com.csu.sms.dto.CoursePerformanceDTO> getCoursePerformances() { return coursePerformances; }
    public void setCoursePerformances(List<com.csu.sms.dto.CoursePerformanceDTO> coursePerformances) { this.coursePerformances = coursePerformances; }
}