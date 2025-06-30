package com.csu.sms.model.course;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudyRecord {
    private Long id;
    private Long userId;
    private Long videoId;
    //观看进度(秒)
    //记录用户观看视频的最新节点，用于续播。
    private Integer progress;
    //学习时长(秒)
    //记录用户总共学习这个视频的时间，用来衡量用户的学习投入。
    private Integer duration;
    private Integer completed;
    //最后学习时间
    private LocalDateTime lastStudyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
