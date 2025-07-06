package com.csu.sms.dto;

import lombok.Data;

/**
 * 考试列表DTO
 * @author CSU Team
 */
@Data
public class ExamListDTO {
    
    /**
     * 考试ID
     */
    private Long id;
    
    /**
     * 考试名称
     */
    private String name;
} 