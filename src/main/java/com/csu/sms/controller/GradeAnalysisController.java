package com.csu.sms.controller;

import com.csu.sms.dto.*;
import com.csu.sms.service.GradeAnalysisService;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ApiResponse;  // 修改import
import com.csu.sms.annotation.LogOperation;
import com.csu.sms.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/grade-analysis")
@CrossOrigin
public class GradeAnalysisController {

    @Autowired
    private GradeAnalysisService gradeAnalysisService;

    /**
     * 获取当前用户的考试记录
     */
    @GetMapping("/my/exam-records")
    @LogOperation(module = "成绩管理", operation = "查询我的考试记录", description = "获取当前用户考试记录")
    public ApiResponse<PageResult<ExamRecordDTO>> getMyExamRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录", "USER_NOT_LOGGED_IN");
        }

        PageResult<ExamRecordDTO> result = gradeAnalysisService.getUserExamRecords(userId, pageNum, pageSize, startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 获取当前用户的学习记录
     */
    @GetMapping("/my/study-records")
    @LogOperation(module = "学习分析", operation = "查询我的学习记录", description = "获取当前用户学习记录")
    public ApiResponse<PageResult<StudyRecordDTO>> getMyStudyRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录", "USER_NOT_LOGGED_IN");
        }

        PageResult<StudyRecordDTO> result = gradeAnalysisService.getUserStudyRecords(userId, pageNum, pageSize, startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 获取当前用户的综合分析
     */
    @GetMapping("/my/analysis")
    @LogOperation(module = "学习分析", operation = "我的综合分析", description = "获取当前用户综合分析数据")
    public ApiResponse<GradeAnalysisDTO> getMyAnalysis(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录", "USER_NOT_LOGGED_IN");
        }

        GradeAnalysisDTO result = gradeAnalysisService.getUserAnalysis(userId, startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 获取当前用户的图表数据
     */
    @GetMapping("/my/chart")
    @LogOperation(module = "学习分析", operation = "获取我的图表数据", description = "获取当前用户学习效果图表数据")
    public ApiResponse<Map<String, Object>> getMyChartData(
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录", "USER_NOT_LOGGED_IN");
        }

        Map<String, Object> result = gradeAnalysisService.getChartData(userId, type, startDate, endDate);
        return ApiResponse.success(result);
    }

    // ================ 保留原有接口（管理员/教师使用） ================

    /**
     * 获取用户考试记录（管理员/教师接口）
     */
    @GetMapping("/exam-records/{userId}")
    @LogOperation(module = "成绩管理", operation = "查询考试记录", description = "获取用户考试记录")
    public ApiResponse<PageResult<ExamRecordDTO>> getUserExamRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        PageResult<ExamRecordDTO> result = gradeAnalysisService.getUserExamRecords(userId, pageNum, pageSize, startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 获取用户学习记录（管理员/教师接口）
     */
    @GetMapping("/study-records/{userId}")
    @LogOperation(module = "学习分析", operation = "查询学习记录", description = "获取用户学习记录")
    public ApiResponse<PageResult<StudyRecordDTO>> getUserStudyRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        PageResult<StudyRecordDTO> result = gradeAnalysisService.getUserStudyRecords(userId, pageNum, pageSize, startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 根据考试ID获取考试记录
     */
    @GetMapping("/exam-records/exam/{examId}")
    @LogOperation(module = "成绩管理", operation = "按考试查询记录", description = "根据考试ID获取考试记录")
    public ApiResponse<PageResult<ExamRecordDTO>> getExamRecordsByExam(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        PageResult<ExamRecordDTO> result = gradeAnalysisService.getExamRecordsByExam(examId, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 获取用户综合分析（管理员/教师接口）
     */
    @GetMapping("/analysis/{userId}")
    @LogOperation(module = "学习分析", operation = "用户综合分析", description = "获取用户综合分析数据")
    public ApiResponse<GradeAnalysisDTO> getUserAnalysis(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        GradeAnalysisDTO result = gradeAnalysisService.getUserAnalysis(userId, startDate, endDate);
        return ApiResponse.success(result);
    }

    /**
     * 获取图表数据（管理员/教师接口）
     */
    @GetMapping("/chart/{userId}")
    @LogOperation(module = "学习分析", operation = "获取图表数据", description = "获取学习效果图表数据")
    public ApiResponse<Map<String, Object>> getChartData(
            @PathVariable Long userId,
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Map<String, Object> result = gradeAnalysisService.getChartData(userId, type, startDate, endDate);
        return ApiResponse.success(result);
    }
}