package com.csu.sms.model.knowledge;

import lombok.Data;
import java.util.Date;

@Data
public class KnowledgeFavorite {
    private Long id;
    private Long userId;
    private Long knowledgeId;
    private String remark;
    private Date favoriteTime;
    private Integer status;
} 