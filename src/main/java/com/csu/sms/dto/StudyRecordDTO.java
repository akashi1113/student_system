package com.csu.sms.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class StudyRecordDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    @NotNull(message = "进度不能为空")
    private Integer progress;

    @NotNull(message = "学习时长不能为空")
    private Integer duration;
    
    // 新增字段，用于显示课程和视频信息
    private Long courseId;
    private String courseTitle;
    private String videoTitle;
    private Integer completed;
    private LocalDateTime lastStudyTime;
    private Integer videoDuration; // 视频总时长
}
