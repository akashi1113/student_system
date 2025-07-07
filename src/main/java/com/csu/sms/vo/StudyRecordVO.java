package com.csu.sms.vo;

import lombok.Data;

@Data
public class StudyRecordVO {
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private Long videoId;
    private String videoTitle;
    //上次播放位置 (秒)，用于断点续传
    private Integer lastPlaybackPosition;
    //最远播放进度 (秒)，用于计算单个视频的完成百分比
    private Integer maxProgress;
    private Boolean isCompleted;
    //视频总时长 (秒)
    private Integer videoDuration;
    //用户在该视频上的累计学习时长 (秒)
    private Integer totalWatchTime;
    //最后一次学习时间
    private String lastStudyTime;
}

