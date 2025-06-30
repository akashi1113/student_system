package com.csu.sms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExperimentDTO {
    private Long id;
    private String name;
    private String subject;
    private String description;
    private Integer duration;

    private Integer status;  // 0-禁用, 1-可预约, 2-已满额, 3-已关闭

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 实验步骤和参数示例数据
    private String steps;
    private String parameters;
}