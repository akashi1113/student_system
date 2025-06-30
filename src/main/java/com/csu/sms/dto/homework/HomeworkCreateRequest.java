package com.csu.sms.dto.homework;

import com.csu.sms.model.homework.Homework;
import com.csu.sms.model.homework.HomeworkQuestion;

import java.util.List;

public class HomeworkCreateRequest {
    private Homework homework;
    private List<HomeworkQuestion> questions;

    // getter和setter
    public Homework getHomework() { return homework; }
    public void setHomework(Homework homework) { this.homework = homework; }
    public List<HomeworkQuestion> getQuestions() { return questions; }
    public void setQuestions(List<HomeworkQuestion> questions) { this.questions = questions; }
}
