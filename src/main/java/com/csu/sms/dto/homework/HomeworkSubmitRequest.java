package com.csu.sms.dto.homework;

import com.csu.sms.model.homework.HomeworkAnswer;

import java.util.List;

public class HomeworkSubmitRequest {
    private Long studentId;
    private List<HomeworkAnswer> answers;

    // getter和setter
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public List<HomeworkAnswer> getAnswers() { return answers; }
    public void setAnswers(List<HomeworkAnswer> answers) { this.answers = answers; }
}
