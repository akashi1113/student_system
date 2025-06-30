package com.csu.sms.model.exam;

import com.csu.sms.persistence.ExamMapper;
import com.csu.sms.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExamTimeoutTask {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamMapper examMapper;

    /**
     * 每分钟检查一次超时的考试
     */
    @Scheduled(fixedRate = 60000) // 60秒执行一次
    public void checkTimeoutExams() {
        try {
            // 查找所有进行中的考试记录
            List<ExamRecord> inProgressRecords = examMapper.findExamRecordsByStatus("IN_PROGRESS");

            for (ExamRecord record : inProgressRecords) {
                if (isExamTimeout(record)) {
                    // 自动提交超时的考试
                    examService.autoSubmitExam(record.getExamId(), record.getUserId(), "TIMEOUT");
                }
            }
        } catch (Exception e) {
            // 记录日志
            System.err.println("检查考试超时失败: " + e.getMessage());
        }
    }

    /**
     * 判断考试是否超时
     */
    private boolean isExamTimeout(ExamRecord record) {
        if (record.getStartTime() == null) {
            return false;
        }

        // 获取考试信息
        var exam = examMapper.findById(record.getExamId());
        if (exam == null || exam.getDuration() == null) {
            return false;
        }

        // 计算考试结束时间
        LocalDateTime examEndTime = record.getStartTime().plusMinutes(exam.getDuration());

        // 检查是否超时
        return LocalDateTime.now().isAfter(examEndTime);
    }
}