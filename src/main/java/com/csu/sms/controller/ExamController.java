package com.csu.sms.controller;

import com.csu.sms.dto.exam.AnswerDTO;
import com.csu.sms.dto.ApiResponse;
import com.csu.sms.dto.exam.ViolationRequest;
import com.csu.sms.model.exam.Exam;
import com.csu.sms.model.exam.ExamRecord;
import com.csu.sms.model.exam.ExamScoreResult;
import com.csu.sms.service.ExamService;
import com.csu.sms.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.csu.sms.annotation.LogOperation;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "http://localhost:5173")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ApiResponse<List<Exam>> getAllExams() {
        return ApiResponse.success(examService.getAllAvailableExams());
    }

    @GetMapping("/bookable")
    public ApiResponse<List<Exam>> getBookableExams() {
        return ApiResponse.success(examService.getBookableExams());
    }

    @GetMapping("/booked")
    public ApiResponse<List<Exam>> getBookedExams(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(examService.getBookedExams(userId));
    }

    @GetMapping("/{examId}")
    public ApiResponse<Exam> getExamById(@PathVariable("examId") Long examId) {
        Exam exam = examService.getExamById(examId);
        return ApiResponse.success(exam);
    }

    @PostMapping("/{examId}/start")
    @LogOperation(module = "考试管理", operation = "开始考试", description = "学生开始考试")
    public ApiResponse<ExamRecord> startExam(@PathVariable Long examId,
                                             Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if(!examService.canStartExam(examId, userId)) {
            return ApiResponse.success("您已经完成该考试，无法再次开始",examService.getExamRecord(examId, userId));
        }
        ExamRecord record = examService.startExam(examId, userId);
        return ApiResponse.success(record);
    }

    @GetMapping("/{examId}/status")
    public ApiResponse<ExamRecord> getExamStatus(@PathVariable Long examId,
                                                 Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        ExamRecord record = examService.getExamRecord(examId, userId);
        return ApiResponse.success(record);
    }

    private Long getUserIdFromAuth(Authentication auth) {
        // 从JWT token中获取用户ID
        return 3L; // 简化处理
    }

    // 提交考试答案
    @PostMapping("/{examId}/submit")
    @LogOperation(module = "考试管理", operation = "提交考试", description = "学生提交考试答案")
    public ApiResponse<ExamScoreResult> submitExam(@PathVariable Long examId,
                                                   @RequestBody List<AnswerDTO> answers,
                                                   Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            // 获取考试记录
            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.error("考试已结束，无法提交答案", "EXAM_ALREADY_ENDED");
            }

            // 保存答案并评分
            questionService.scoreAnswersWithAI(examRecord.getId(), answers);

            // 计算总分
            int totalScore = examService.calculateTotalScore(examRecord.getId());

            // 计算实际用时
            if (examRecord.getStartTime() != null) {
                long duration = java.time.Duration.between(
                        examRecord.getStartTime(),
                        LocalDateTime.now()
                ).toSeconds();
                examRecord.setDuration((int) duration);
            }

            // 更新考试记录
            examRecord.setScore(totalScore);
            examRecord.setStatus("SUBMITTED");
            examRecord.setSubmitTime(LocalDateTime.now());
            examService.updateExamRecord(examRecord);

            // 构建返回结果
            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(totalScore);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6)); // 60%及格
            result.setPassed(totalScore >= result.getPassingScore());

            return ApiResponse.success("提交考试成功", result);

        } catch (Exception e) {
            return ApiResponse.error("提交考试失败: " + e.getMessage());
        }
    }

    // 记录违规行为
    @PostMapping("/{examId}/violation")
    @LogOperation(module = "考试管理", operation = "记录违规", description = "记录考试违规行为")
    public ApiResponse<Void> recordViolation(@PathVariable Long examId,
                                             @RequestBody ViolationRequest violation,
                                             Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            // 获取考试记录
            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.success("考试已结束", null);
            }

            // 增加违规次数
            int currentViolations = examRecord.getViolationCount() != null ? examRecord.getViolationCount() : 0;
            examRecord.setViolationCount(currentViolations + 1);

            // 检查是否达到违规上限
            if (examRecord.getViolationCount() >= 3) {
                // 自动提交考试
                examService.autoSubmitExam(examId, userId, "VIOLATION");
                return ApiResponse.success("违规次数过多，考试已自动提交", null);
            } else {
                // 只更新违规次数
                examService.updateExamRecord(examRecord);
                return ApiResponse.success("违规记录已保存", null);
            }

        } catch (Exception e) {
            return ApiResponse.error("记录违规失败: " + e.getMessage());
        }
    }

    // 考试超时处理
    @PostMapping("/{examId}/timeout")
    @LogOperation(module = "考试管理", operation = "超时处理", description = "考试超时自动提交")
    public ApiResponse<ExamScoreResult> handleTimeout(@PathVariable Long examId,
                                                      Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            // 自动提交考试
            ExamRecord examRecord = examService.autoSubmitExam(examId, userId, "TIMEOUT");

            // 构建返回结果
            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(examRecord.getScore() != null ? examRecord.getScore() : 0);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6));
            result.setPassed(result.getTotalScore() >= result.getPassingScore());

            return ApiResponse.success("考试时间到，已自动提交", result);

        } catch (Exception e) {
            return ApiResponse.error("处理考试超时失败: " + e.getMessage());
        }
    }

    // 获取考试成绩
    @GetMapping("/{examId}/score")
    public ApiResponse<ExamScoreResult> getExamScore(@PathVariable Long examId,
                                                     Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if ("IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.error("考试尚未结束", "EXAM_IN_PROGRESS");
            }

            // 构建成绩结果
            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(examRecord.getScore() != null ? examRecord.getScore() : 0);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6));
            result.setPassed(result.getTotalScore() >= result.getPassingScore());

            return ApiResponse.success("获取成绩成功", result);

        } catch (Exception e) {
            return ApiResponse.error("获取成绩失败: " + e.getMessage());
        }
    }
}