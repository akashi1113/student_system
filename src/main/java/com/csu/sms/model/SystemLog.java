package com.csu.sms.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统日志实体类
 * 记录系统级别的事件
 */
@Data
public class SystemLog {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 日志级别（INFO/WARN/ERROR）
     */
    private String level;
    
    /**
     * 日志类型
     */
    private String type;
    
    /**
     * 日志标题
     */
    private String title;
    
    /**
     * 日志内容
     */
    private String content;
    
    /**
     * 来源模块
     */
    private String source;
    
    /**
     * 堆栈信息
     */
    private String stackTrace;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 