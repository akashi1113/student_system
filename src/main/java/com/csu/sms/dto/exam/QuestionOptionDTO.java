package com.csu.sms.dto.exam;


import jakarta.validation.constraints.NotBlank;

public class QuestionOptionDTO {

    private Long id;

    @NotBlank(message = "选项内容不能为空")
    private String content;

    private Boolean isCorrect = false;

    private String optionLabel;

    private Integer orderNum;

    public QuestionOptionDTO() {}

    public QuestionOptionDTO(String content, Boolean isCorrect, String optionLabel) {
        this.content = content;
        this.isCorrect = isCorrect;
        this.optionLabel = optionLabel;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
