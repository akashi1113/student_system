package com.csu.sms.vo;

import lombok.Data;

@Data
public class OperationLogExportVO {
    private Long id;
    private String username;
    private String module;
    private String operation;
    private String description;
    private String status;
    private String ipAddress;
    private String createTime;
} 