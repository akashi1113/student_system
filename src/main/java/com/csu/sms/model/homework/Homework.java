package com.csu.sms.model.homework;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Homework {
    private Long id;
    private String title;
    private String description;
    private Long courseId;
    private Long teacherId;
    private String homeworkType;
    private BigDecimal totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer allowResubmit;
    private Integer maxSubmitTimes;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 关联信息
    private String teacherName;
    private String courseTitle;  // 新增：课程标题
    private List<HomeworkQuestion> questions;

    // 构造方法
    public Homework() {}

    // getter和setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getHomeworkType() { return homeworkType; }
    public void setHomeworkType(String homeworkType) { this.homeworkType = homeworkType; }

    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getAllowResubmit() { return allowResubmit; }
    public void setAllowResubmit(Integer allowResubmit) { this.allowResubmit = allowResubmit; }

    public Integer getMaxSubmitTimes() { return maxSubmitTimes; }
    public void setMaxSubmitTimes(Integer maxSubmitTimes) { this.maxSubmitTimes = maxSubmitTimes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public List<HomeworkQuestion> getQuestions() { return questions; }
    public void setQuestions(List<HomeworkQuestion> questions) { this.questions = questions; }
}