package com.csu.sms.vo;

import lombok.Data;

@Data
public class StudyRecordVO {
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private Long videoId;
    private String videoTitle;
    private Integer lastPlaybackPosition;
    private Integer maxProgress;
    private Boolean isCompleted;
    private Integer progress;
    private Integer videoDuration;
    private Integer totalWatchTime;
    private String lastStudyTime;

    // getter/setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public String getVideoTitle() { return videoTitle; }
    public void setVideoTitle(String videoTitle) { this.videoTitle = videoTitle; }
    public Integer getLastPlaybackPosition() { return lastPlaybackPosition; }
    public void setLastPlaybackPosition(Integer lastPlaybackPosition) { this.lastPlaybackPosition = lastPlaybackPosition; }
    public Integer getMaxProgress() { return maxProgress; }
    public void setMaxProgress(Integer maxProgress) { this.maxProgress = maxProgress; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public Integer getVideoDuration() { return videoDuration; }
    public void setVideoDuration(Integer videoDuration) { this.videoDuration = videoDuration; }
    public Integer getTotalWatchTime() { return totalWatchTime; }
    public void setTotalWatchTime(Integer totalWatchTime) { this.totalWatchTime = totalWatchTime; }
    public String getLastStudyTime() { return lastStudyTime; }
    public void setLastStudyTime(String lastStudyTime) { this.lastStudyTime = lastStudyTime; }
}
