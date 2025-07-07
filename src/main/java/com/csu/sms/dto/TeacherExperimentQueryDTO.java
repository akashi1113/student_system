package com.csu.sms.dto;

import lombok.Data;

@Data
public class TeacherExperimentQueryDTO {
    private String name;
    private String subject;
    private Integer status;
    private Boolean isPublished;
    private Long teacherId;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
