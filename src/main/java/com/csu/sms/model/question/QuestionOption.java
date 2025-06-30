package com.csu.sms.model.question;

public class QuestionOption {
    private Long id;
    private Long questionId;
    private String content;
    private Boolean isCorrect;
    private String optionLabel; // A, B, C, D
    private Integer orderNum;

    public QuestionOption() {}

    public QuestionOption(Long questionId, String content, Boolean isCorrect, String optionLabel) {
        this.questionId = questionId;
        this.content = content;
        this.isCorrect = isCorrect;
        this.optionLabel = optionLabel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}
