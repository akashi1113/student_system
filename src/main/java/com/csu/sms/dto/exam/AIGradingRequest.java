package com.csu.sms.dto.exam;

public class AIGradingRequest {
    private Long questionId;
    private String questionType;
    private String questionContent;
    private String studentAnswer;
    private String referenceAnswer;
    private Integer maxScore;

    // 构造函数、getter、setter
    public AIGradingRequest() {}

    public AIGradingRequest(Long questionId, String questionType, String questionContent,
                            String studentAnswer, String referenceAnswer, Integer maxScore) {
        this.questionId = questionId;
        this.questionType = questionType;
        this.questionContent = questionContent;
        this.studentAnswer = studentAnswer;
        this.referenceAnswer = referenceAnswer;
        this.maxScore = maxScore;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }
}