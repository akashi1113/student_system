package com.csu.sms.dto.exam;

public class QuestionOptionResponseDTO {
    private Long id;
    private String content;
    private String optionLabel;

    public QuestionOptionResponseDTO() {}

    public QuestionOptionResponseDTO(Long id, String content, String optionLabel) {
        this.id = id;
        this.content = content;
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

    public String getOptionLabel() {
        return optionLabel;
    }

    public void setOptionLabel(String optionLabel) {
        this.optionLabel = optionLabel;
    }
}