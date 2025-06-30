package com.csu.sms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CourseVO {
    private Long id;
    private String title;
    private String description;
    private String coverImg;
    private String teacherName;
    private Integer videoCount;
    private Integer studyProgress;
}
