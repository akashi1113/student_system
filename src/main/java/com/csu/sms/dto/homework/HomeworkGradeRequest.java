package com.csu.sms.dto.homework;

import com.csu.sms.model.homework.HomeworkAnswer;

import java.util.List;

public class HomeworkGradeRequest {
    private List<HomeworkAnswer> answers;
    private String feedback;
    private Long teacherId;

    // getter和setter
    public List<HomeworkAnswer> getAnswers() { return answers; }
    public void setAnswers(List<HomeworkAnswer> answers) { this.answers = answers; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}
