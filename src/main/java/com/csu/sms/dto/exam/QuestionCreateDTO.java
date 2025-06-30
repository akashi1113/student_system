package com.csu.sms.dto.exam;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class QuestionCreateDTO {

    @NotNull(message = "考试ID不能为空")
    private Long examId;

    @NotBlank(message = "题目内容不能为空")
    private String content;

    @Pattern(regexp = "SINGLE|MULTIPLE|JUDGE|TEXT", message = "题目类型必须是 SINGLE, MULTIPLE, JUDGE, TEXT 之一")
    private String type;

    @NotNull(message = "题目分数不能为空")
    @Min(value = 1, message = "题目分数不能小于1")
    private Integer score;

    private Integer orderNum;

    private String analysis;

    @Pattern(regexp = "EASY|MEDIUM|HARD", message = "难度必须是 EASY, MEDIUM, HARD 之一")
    private String difficulty = "MEDIUM";

    private List<QuestionOptionDTO> options;

    // 构造函数
    public QuestionCreateDTO() {}

    // Getter 和 Setter 方法
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

    public List<QuestionOptionDTO> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDTO> options) {
        this.options = options;
    }
}