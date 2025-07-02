package com.csu.sms.controller;

import com.csu.sms.dto.*;
import com.csu.sms.service.GradeAnalysisService;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ApiResponse;  // 修改import
import com.csu.sms.annotation.LogOperation;
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
     * 获取用户考试记录
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
     * 获取用户学习记录
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
     * 获取用户综合分析
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
     * 获取图表数据
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