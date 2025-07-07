package com.csu.sms.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class StudyRecordDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "视频ID不能为空")
    private Long videoId;

    //前端播放器当前时间点（秒）
    private Integer currentPlaybackPosition;

    //本次上报周期内的观看时长（秒）
    private Integer watchDurationSinceLastSave;

    @NotNull(message = "视频总时长不能为空")
    private Integer videoDuration;
    
    // 新增字段，用于显示课程和视频信息
    private Long courseId;
    private String courseTitle;
    private String videoTitle;
    private Integer completed;
    private java.time.LocalDateTime lastStudyTime;
    private Integer progress;

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }
}
