package com.csu.sms.model.question;

import java.time.LocalDateTime;
import java.util.List;

public class Question {
    private Long id;
    private Long examId;
    private String content;
    private String type;
    private Integer score;
    private Integer orderNum;
    private String analysis; // 题目解析
    private String difficulty; // EASY, MEDIUM, HARD
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuestionOption> options;

    public Question() {}

    public Question(Long examId, String content, String type, Integer score) {
        this.examId = examId;
        this.content = content;
        this.type = type;
        this.score = score;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", examId=" + examId +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", score=" + score +
                ", orderNum=" + orderNum +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}