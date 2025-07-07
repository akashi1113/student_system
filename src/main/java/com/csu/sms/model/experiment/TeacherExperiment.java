package com.csu.sms.model.experiment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherExperiment {
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
