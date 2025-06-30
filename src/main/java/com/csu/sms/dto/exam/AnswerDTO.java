package com.csu.sms.dto.exam;


import jakarta.validation.constraints.NotNull;

public class AnswerDTO {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    private String answer; // 学生的答案

    public AnswerDTO() {}

    public AnswerDTO(Long questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    // Getter 和 Setter 方法
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
}
