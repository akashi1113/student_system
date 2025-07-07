package com.csu.sms.dto;

import lombok.Data;

@Data
public class ExperimentTemplateDTO {
    private Long id;
    private Long experimentId;
    private String purpose;
    private String content;
    private String method;
    private String steps; // JSON字符串
    private String conclusionGuide;
    private Long createdBy;
}
