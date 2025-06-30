package com.csu.sms.dto.exam;

import java.util.List;

public class QuestionResponseDTO {
    private Long id;
    private String content;
    private String type;
    private Integer score;
    private Integer orderNum;
    private String difficulty;
    private List<QuestionOptionResponseDTO> options;

    public QuestionResponseDTO() {}

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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<QuestionOptionResponseDTO> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionResponseDTO> options) {
        this.options = options;
    }
}