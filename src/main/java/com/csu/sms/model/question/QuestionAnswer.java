package com.csu.sms.model.question;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionAnswer {
    private Long id;
    private Long submissionId;
    private Long questionId;
    private String answer;
    private Integer score;
    private String feedback;
    private LocalDateTime createdAt;
}
