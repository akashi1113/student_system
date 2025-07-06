package com.csu.sms.dto.exam;

import java.util.List;

public class CreateExamRequest {
    private Long courseId;
    private String title;
    private String description;
    private Integer duration; // 考试时长（分钟）
    private String examMode; // ONLINE 或 OFFLINE
    private String type; // 考试类型
    private Integer totalScore;
    private Integer passingScore;
    private Integer maxAttempts;
    private List<QuestionCreateDTO> questions; // 线上考试需要的题目


    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getExamMode() { return examMode; }
    public void setExamMode(String examMode) { this.examMode = examMode; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public List<QuestionCreateDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionCreateDTO> questions) { this.questions = questions; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
}