package com.csu.sms.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AIBookRecommendation {
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseName;
    private Double score;
    private String bookTitle;
    private String bookAuthor;
    private String doubanUrl;
    private String recommendationReason;
    private Integer isRead;
    private LocalDateTime createTime;
} 