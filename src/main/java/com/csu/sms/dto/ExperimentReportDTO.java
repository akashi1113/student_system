package com.csu.sms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExperimentReportDTO {
    private Long id;
    private Long recordId;
    private Long templateId;
    private String content;
    private String filePath;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 关联实验记录
    private ExperimentRecordDTO record;
}