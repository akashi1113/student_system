package com.csu.sms.model.question;

public class AnswerStatistics {
    private Long examRecordId;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer correctAnswers;
    private Integer totalScore;
    private Integer earnedScore;
    private Double accuracy;

    // 构造函数和getter/setter方法
    public AnswerStatistics() {}

    public Long getExamRecordId() { return examRecordId; }
    public void setExamRecordId(Long examRecordId) { this.examRecordId = examRecordId; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getAnsweredQuestions() { return answeredQuestions; }
    public void setAnsweredQuestions(Integer answeredQuestions) { this.answeredQuestions = answeredQuestions; }

    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Integer getEarnedScore() { return earnedScore; }
    public void setEarnedScore(Integer earnedScore) { this.earnedScore = earnedScore; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}
