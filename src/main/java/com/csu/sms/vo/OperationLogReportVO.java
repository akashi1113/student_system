package com.csu.sms.vo;

import lombok.Data;

@Data
public class OperationLogReportVO {
    private Long userId;
    private String username;
    private String operation;
    private String module;
    private Integer count;
    private Integer successCount;
    private Integer failedCount;
    private String lastOperationTime;
} 