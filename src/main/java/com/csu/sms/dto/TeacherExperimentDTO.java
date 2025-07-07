package com.csu.sms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherExperimentDTO {
    private Long id;
    private String name;
    private String subject;
    private String description;
    private Integer duration;
    private String location;
    private Integer status;
    private Boolean isPublished;
    private String steps;
    private Long createdBy;
    private Long studentExperimentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
