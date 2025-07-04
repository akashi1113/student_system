package com.csu.sms.vo;

public class StudyRecordVO {
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private Long videoId;
    private String videoTitle;
    private Integer progress;
    private Integer duration;
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
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getLastStudyTime() { return lastStudyTime; }
    public void setLastStudyTime(String lastStudyTime) { this.lastStudyTime = lastStudyTime; }
} 