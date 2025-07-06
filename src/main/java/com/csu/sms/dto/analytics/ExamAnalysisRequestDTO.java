package com.csu.sms.dto.analytics;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试分析请求DTO
 * @author CSU Team
 */
@Data
public class ExamAnalysisRequestDTO {
    
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 课程ID列表（可选）
     */
    private List<Long> courseIds;
    
    /**
     * 考试ID列表（可选）
     */
    private List<Long> examIds;
    
    /**
     * 学生ID列表（可选）
     */
    private List<Long> studentIds;
    
    /**
     * 教师ID（用于权限控制）
     */
    private Long teacherId;
    
    /**
     * 分析类型：OVERVIEW, DISTRIBUTION, TREND, COURSE_COMPARISON
     */
    private String analysisType;
    
    /**
     * 时间粒度：DAY, WEEK, MONTH, SEMESTER
     */
    private String timeGranularity;
} 