package com.csu.sms.model.course;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Course {
    private Long id;
    private String title;
    private String description;
    //封面图
    private String coverImg;
    private String teacherName;
    //状态 0-上架 1-下架
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

