package com.csu.sms.model.question;

import java.time.LocalDateTime;

//考试用
public class AnswerRecord {
    private Long id;
    private Long examRecordId;
    private Long questionId;
    private String answer; // 学生答案
    private String correctAnswer; // 正确答案
    private Integer score; // 得分
    private Boolean isCorrect;
    private LocalDateTime answeredAt;
    private String aiFeedback;
    private Double aiScoreRatio;
    private String gradingMethod;

    public AnswerRecord() {}

    public AnswerRecord(Long examRecordId, Long questionId, String answer) {
        this.examRecordId = examRecordId;
        this.questionId = questionId;
        this.answer = answer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamRecordId() {
        return examRecordId;
    }

    public void setExamRecordId(Long examRecordId) {
        this.examRecordId = examRecordId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public Double getAiScoreRatio() {
        return aiScoreRatio;
    }

    public void setAiScoreRatio(Double aiScoreRatio) {
        this.aiScoreRatio = aiScoreRatio;
    }

    public String getGradingMethod() {
        return gradingMethod;
    }

    public void setGradingMethod(String gradingMethod) {
        this.gradingMethod = gradingMethod;
    }
}
