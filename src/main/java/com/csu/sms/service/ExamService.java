package com.csu.sms.service;

import com.csu.sms.model.exam.Exam;
import com.csu.sms.model.exam.ExamRecord;
import com.csu.sms.model.question.Question;
import com.csu.sms.persistence.ExamMapper;
import com.csu.sms.persistence.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    public List<Exam> getAllAvailableExams() {
        return examMapper.findAvailableExams();
    }

    public Exam getExamById(Long examId) {
        Exam exam = examMapper.findById(examId);
        if (exam != null) {
            List<Question> questions = examMapper.findQuestionsByExamId(examId);
            exam.setQuestions(questions);
        }
        return exam;
    }

    @Transactional
    public ExamRecord startExam(Long examId, Long userId) {
        // 检查是否已经开始考试
        ExamRecord existingRecord = examMapper.findExamRecord(examId, userId);
        if (existingRecord != null && !"TIMEOUT".equals(existingRecord.getStatus())) {
            return existingRecord;
        }

        // 创建新的考试记录
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setUserId(userId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("IN_PROGRESS");
        record.setViolationCount(0);

        examMapper.insertExamRecord(record);
        return record;
    }

    public void recordViolation(Long examId, Long userId, String violationType) {
        ExamRecord record = examMapper.findExamRecord(examId, userId);
        if (record != null) {
            record.setViolationCount(record.getViolationCount() + 1);
            examMapper.updateExamRecord(record);

            // 违规次数过多时自动提交
            if (record.getViolationCount() >= 3) {
                autoSubmitExam(examId, userId);
            }
        }
    }

    private void autoSubmitExam(Long examId, Long userId) {
        ExamRecord record = examMapper.findExamRecord(examId, userId);
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus("TIMEOUT");
        examMapper.updateExamRecord(record);
    }

    public ExamRecord getExamRecord(Long examId, Long userId) {
        return examMapper.findExamRecord(examId, userId);
    }

    @Transactional
    public ExamRecord updateExamRecord(ExamRecord examRecord) {
        if (examRecord == null || examRecord.getId() == null) {
            throw new RuntimeException("考试记录或ID不能为空");
        }

        // 检查考试记录是否存在
        ExamRecord existingRecord = examMapper.findExamRecordById(examRecord.getId());
        if (existingRecord == null) {
            throw new RuntimeException("考试记录不存在");
        }

        // 更新考试记录
        int result = examMapper.updateExamRecord(examRecord);
        if (result <= 0) {
            throw new RuntimeException("更新考试记录失败");
        }

        // 返回更新后的记录
        return examMapper.findExamRecordById(examRecord.getId());
    }

    // 计算考试记录的总分
    public int calculateTotalScore(Long examRecordId) {
        if (examRecordId == null) {
            throw new RuntimeException("考试记录ID不能为空");
        }

        // 检查考试记录是否存在
        ExamRecord examRecord = examMapper.findExamRecordById(examRecordId);
        if (examRecord == null) {
            throw new RuntimeException("考试记录不存在");
        }

        // 计算总分
        Integer totalScore = questionMapper.calculateTotalScore(examRecordId);
        return totalScore != null ? totalScore : 0;
    }

    // 自动提交考试（时间到或违规次数过多）
    @Transactional
    public ExamRecord autoSubmitExam(Long examId, Long userId, String reason) {
        ExamRecord examRecord = examMapper.findExamRecord(examId, userId);
        if (examRecord == null) {
            throw new RuntimeException("考试记录不存在");
        }

        if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
            return examRecord; // 已经结束的考试不需要再次提交
        }

        // 计算实际用时
        if (examRecord.getStartTime() != null) {
            long duration = java.time.Duration.between(
                    examRecord.getStartTime(),
                    java.time.LocalDateTime.now()
            ).toSeconds();
            examRecord.setDuration((int) duration);
        }

        // 设置提交时间和状态
        examRecord.setSubmitTime(java.time.LocalDateTime.now());

        // 根据原因设置不同的状态
        if ("TIMEOUT".equals(reason)) {
            examRecord.setStatus("TIMEOUT");
        } else if ("VIOLATION".equals(reason)) {
            examRecord.setStatus("SUBMITTED");
        } else {
            examRecord.setStatus("SUBMITTED");
        }

        // 计算并更新总分
        int totalScore = calculateTotalScore(examRecord.getId());
        examRecord.setScore(totalScore);

        // 更新考试记录
        updateExamRecord(examRecord);

        return examRecord;
    }
}