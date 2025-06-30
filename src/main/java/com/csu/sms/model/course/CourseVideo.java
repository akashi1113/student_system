package com.csu.sms.model.course;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseVideo {
    private Long id;
    private Long courseId;
    private String title;
    private String url;
    //视频时长(秒)
    private Integer duration;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
