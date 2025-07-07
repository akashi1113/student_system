package com.csu.sms.model.course;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyRecord {
    private Long id;
    private Long userId;
    private Long videoId;
    private Integer videoDuration;
    //上次播放位置（秒),实现断点续传，记录用户离开时的播放时间点。
    private Integer lastPlaybackPosition;
    //最远播放进度（秒），记录用户历史到达的最远时间点，不受拖动回退影响。
    private Integer maxProgress;
    //观看进度(秒)
    //记录用户观看视频的最新节点，用于续播。
    private Integer progress;
    //是否已完成
    //一旦为 `true`，**永不回退**。
    private Boolean isCompleted;
    //累计观看时长（秒）
    //记录用户在这个视频上总共花了多长时间，可用于数据分析。
    private Integer totalWatchTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

