package com.csu.sms.dto;

import java.time.LocalDateTime;

/**
 * 学习记录DTO
 */
public class StudyRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long videoId;
    private String videoTitle;
    private Long courseId;
    private String courseTitle;
    private Integer progress;
    private Integer duration;
    private Boolean completed;
    private LocalDateTime lastStudyTime;

    // 视频信息
    private Integer videoDuration;

    // 构造函数
    public StudyRecordDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDateTime getLastStudyTime() { return lastStudyTime; }
    public void setLastStudyTime(LocalDateTime lastStudyTime) { this.lastStudyTime = lastStudyTime; }

    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }

    // 计算观看进度百分比 (使用Java 17语法)
    public double getProgressPercentage() {
        return (progress != null && videoDuration != null && videoDuration > 0)
                ? (double) progress / videoDuration * 100
                : 0.0;
    }

//    // 格式化学习时长
//    public String getFormattedDuration() {
//        if (duration == null || duration == 0) return "0分钟";
//
//        int hours = duration / 3600;
//        int minutes = (duration % 3600) / 60;
//        int seconds = duration % 60;
//
//        return switch (true) {
//            case hours > 0 -> "%d小时%d分钟".formatted(hours, minutes);
//            case minutes > 0 -> "%d分%d秒".formatted(minutes, seconds);
//            default -> "%d秒".formatted(seconds);
//        };
//    }
}