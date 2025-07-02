package com.csu.sms.dto.exam;

public class QuestionAnalysisDTO {
    private Long questionId;
    private String content;
    private String type;
    private Integer score; // 题目总分
    private Integer earnedScore; // 学生得分
    private String studentAnswer; // 学生答案
    private String correctAnswer; // 正确答案
    private String analysis; // 题目解析
    private Boolean isCorrect;
    private String aiFeedback;
    private Double aiScoreRatio;

    public QuestionAnalysisDTO() {}

    // Getter 和 Setter 方法
    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
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

    public Integer getEarnedScore() {
        return earnedScore;
    }

    public void setEarnedScore(Integer earnedScore) {
        this.earnedScore = earnedScore;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
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
}