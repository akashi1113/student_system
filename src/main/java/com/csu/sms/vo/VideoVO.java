package com.csu.sms.vo;

import lombok.Data;

@Data
public class VideoVO {
    private Long id;
    private Long courseId;
    private String title;
    private String url;
    private Integer duration;
    private Integer sort;
    private Integer progress;  // 当前用户的观看进度(秒)
    private Boolean completed; // 是否已完成观看
    private String courseName; // 所属课程名称
}
