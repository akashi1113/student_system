package com.csu.sms.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * AI学习建议DTO
 */
public class AILearningSuggestionDTO {

    /**
     * 请求参数
     */
    public static class Request {
        private Long userId;
        private String timePeriod;
        private Double averageScore;
        private Long totalStudyTime;
        private Double examPassRate;
        private List<StudyRecordInfo> studyRecords;
        private List<ExamRecordInfo> examRecords;
        private Map<String, Double> timeDistribution;
        private String mostActivePeriod;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getTimePeriod() { return timePeriod; }
        public void setTimePeriod(String timePeriod) { this.timePeriod = timePeriod; }

        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

        public Long getTotalStudyTime() { return totalStudyTime; }
        public void setTotalStudyTime(Long totalStudyTime) { this.totalStudyTime = totalStudyTime; }

        public Double getExamPassRate() { return examPassRate; }
        public void setExamPassRate(Double examPassRate) { this.examPassRate = examPassRate; }

        public List<StudyRecordInfo> getStudyRecords() { return studyRecords; }
        public void setStudyRecords(List<StudyRecordInfo> studyRecords) { this.studyRecords = studyRecords; }

        public List<ExamRecordInfo> getExamRecords() { return examRecords; }
        public void setExamRecords(List<ExamRecordInfo> examRecords) { this.examRecords = examRecords; }

        public Map<String, Double> getTimeDistribution() { return timeDistribution; }
        public void setTimeDistribution(Map<String, Double> timeDistribution) { this.timeDistribution = timeDistribution; }

        public String getMostActivePeriod() { return mostActivePeriod; }
        public void setMostActivePeriod(String mostActivePeriod) { this.mostActivePeriod = mostActivePeriod; }
    }

    /**
     * 学习记录信息
     */
    public static class StudyRecordInfo {
        private Long id;
        private String courseTitle;
        private String videoTitle;
        private Double progressPercentage;
        private Long duration;
        private LocalDateTime lastStudyTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCourseTitle() { return courseTitle; }
        public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

        public String getVideoTitle() { return videoTitle; }
        public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }

        public Double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(Double progressPercentage) { this.progressPercentage = progressPercentage; }

        public Long getDuration() { return duration; }
        public void setDuration(Long duration) { this.duration = duration; }

        public LocalDateTime getLastStudyTime() { return lastStudyTime; }
        public void setLastStudyTime(LocalDateTime lastStudyTime) { this.lastStudyTime = lastStudyTime; }
    }

    /**
     * 考试记录信息
     */
    public static class ExamRecordInfo {
        private Long id;
        private String examTitle;
        private Integer attemptNumber;
        private Integer score;
        private LocalDateTime startTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getExamTitle() { return examTitle; }
        public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

        public Integer getAttemptNumber() { return attemptNumber; }
        public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    }

    /**
     * 学习建议
     */
    public static class Suggestion {
        private String type; // improve, optimize, maintain
        private String title;
        private String content;
        private String priority; // high, medium, low

        public Suggestion() {}

        public Suggestion(String type, String title, String content, String priority) {
            this.type = type;
            this.title = title;
            this.content = content;
            this.priority = priority;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
} 