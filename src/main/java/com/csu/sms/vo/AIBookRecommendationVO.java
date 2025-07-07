package com.csu.sms.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AIBookRecommendationVO {
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