package com.csu.sms.controller;

import com.csu.sms.dto.*;
import com.csu.sms.service.GradeAnalysisService;
import com.csu.sms.util.PageResult;
import com.csu.sms.common.ApiResponse;  // 修改import
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
    public ApiResponse<Map<String, Object>> getChartData(
            @PathVariable Long userId,
            @RequestParam String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        Map<String, Object> result = gradeAnalysisService.getChartData(userId, type, startDate, endDate);
        return ApiResponse.success(result);
    }
}