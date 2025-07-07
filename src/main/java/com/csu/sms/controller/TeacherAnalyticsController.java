package com.csu.sms.controller;

import com.csu.sms.annotation.RequireTeacher;
import com.csu.sms.common.ApiResponse;
import com.csu.sms.dto.analytics.ExamAnalysisRequestDTO;
import com.csu.sms.dto.analytics.ExamAnalysisResponseDTO;
import com.csu.sms.service.TeacherAnalyticsService;
import com.csu.sms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 教师分析控制器
 * @author CSU Team
 */
@RestController
@RequestMapping("/api/teacher/analytics")
@RequiredArgsConstructor
@Slf4j
public class TeacherAnalyticsController {

    private final TeacherAnalyticsService teacherAnalyticsService;

    /**
     * 获取总体概览统计
     */
    @PostMapping("/overview")
    @RequireTeacher
    public ApiResponse<ExamAnalysisResponseDTO.OverviewStats> getOverviewStats(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求总体统计", UserContext.getCurrentUserId());
        
        try {
            // 设置当前用户ID
            request.setTeacherId(UserContext.getCurrentUserId());
            
            ExamAnalysisResponseDTO.OverviewStats stats = teacherAnalyticsService.getOverviewStats(request);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取总体统计失败", e);
            return ApiResponse.error("获取总体统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取成绩分布分析
     */
    @PostMapping("/score-distribution")
    @RequireTeacher
    public ApiResponse<ExamAnalysisResponseDTO.ScoreDistribution> getScoreDistribution(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求成绩分布分析", UserContext.getCurrentUserId());
        
        try {
            request.setTeacherId(UserContext.getCurrentUserId());
            
            ExamAnalysisResponseDTO.ScoreDistribution distribution = teacherAnalyticsService.getScoreDistribution(request);
            return ApiResponse.success(distribution);
        } catch (Exception e) {
            log.error("获取成绩分布分析失败", e);
            return ApiResponse.error("获取成绩分布分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取课程对比分析
     */
    @PostMapping("/course-comparison")
    @RequireTeacher
    public ApiResponse<java.util.List<ExamAnalysisResponseDTO.CourseComparison>> getCourseComparisons(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求课程对比分析", UserContext.getCurrentUserId());
        
        try {
            request.setTeacherId(UserContext.getCurrentUserId());
            
            java.util.List<ExamAnalysisResponseDTO.CourseComparison> comparisons = 
                    teacherAnalyticsService.getCourseComparisons(request);
            return ApiResponse.success(comparisons);
        } catch (Exception e) {
            log.error("获取课程对比分析失败", e);
            return ApiResponse.error("获取课程对比分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取考试对比分析
     */
    @PostMapping("/exam-comparison")
    @RequireTeacher
    public ApiResponse<java.util.List<ExamAnalysisResponseDTO.ExamComparison>> getExamComparisons(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求考试对比分析", UserContext.getCurrentUserId());
        
        try {
            request.setTeacherId(UserContext.getCurrentUserId());
            
            java.util.List<ExamAnalysisResponseDTO.ExamComparison> comparisons = 
                    teacherAnalyticsService.getExamComparisons(request);
            return ApiResponse.success(comparisons);
        } catch (Exception e) {
            log.error("获取考试对比分析失败", e);
            return ApiResponse.error("获取考试对比分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取趋势分析
     */
    @PostMapping("/trend")
    @RequireTeacher
    public ApiResponse<java.util.List<ExamAnalysisResponseDTO.TrendData>> getTrendAnalysis(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求趋势分析", UserContext.getCurrentUserId());
        
        try {
            request.setTeacherId(UserContext.getCurrentUserId());
            
            java.util.List<ExamAnalysisResponseDTO.TrendData> trendData = 
                    teacherAnalyticsService.getTrendAnalysis(request);
            return ApiResponse.success(trendData);
        } catch (Exception e) {
            log.error("获取趋势分析失败", e);
            return ApiResponse.error("获取趋势分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生排名
     */
    @PostMapping("/student-rankings")
    @RequireTeacher
    public ApiResponse<java.util.List<ExamAnalysisResponseDTO.StudentRanking>> getStudentRankings(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求学生排名", UserContext.getCurrentUserId());
        
        try {
            request.setTeacherId(UserContext.getCurrentUserId());
            
            java.util.List<ExamAnalysisResponseDTO.StudentRanking> rankings = 
                    teacherAnalyticsService.getStudentRankings(request);
            return ApiResponse.success(rankings);
        } catch (Exception e) {
            log.error("获取学生排名失败", e);
            return ApiResponse.error("获取学生排名失败：" + e.getMessage());
        }
    }

    /**
     * 获取完整分析报告 (POST)
     */
    @PostMapping("/complete-analysis")
    @RequireTeacher
    public ApiResponse<ExamAnalysisResponseDTO> getCompleteAnalysis(
            @Valid @RequestBody ExamAnalysisRequestDTO request) {
        log.info("用户 {} 请求完整分析报告", UserContext.getCurrentUserId());
        
        try {
            request.setTeacherId(UserContext.getCurrentUserId());
            
            ExamAnalysisResponseDTO analysis = teacherAnalyticsService.getCompleteAnalysis(request);
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            log.error("获取完整分析报告失败", e);
            return ApiResponse.error("获取完整分析报告失败：" + e.getMessage());
        }
    }

    /**
     * 获取完整分析报告 (GET)
     */
    @GetMapping("/complete-analysis")
    @RequireTeacher
    public ApiResponse<ExamAnalysisResponseDTO> getCompleteAnalysisByGet(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String courseIds,
            @RequestParam(required = false) String examIds,
            @RequestParam(required = false) String studentIds,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String timeGranularity) {
        log.info("用户 {} 请求完整分析报告 (GET)", UserContext.getCurrentUserId());
        
        try {
            ExamAnalysisRequestDTO request = new ExamAnalysisRequestDTO();
            request.setTeacherId(UserContext.getCurrentUserId());
            
            // 解析时间参数
            if (startTime != null && !startTime.trim().isEmpty()) {
                request.setStartTime(java.time.LocalDateTime.parse(startTime.replace(" ", "T")));
            } else {
                request.setStartTime(java.time.LocalDateTime.now().minusDays(30));
            }
            
            if (endTime != null && !endTime.trim().isEmpty()) {
                request.setEndTime(java.time.LocalDateTime.parse(endTime.replace(" ", "T")));
            } else {
                request.setEndTime(java.time.LocalDateTime.now());
            }
            
            // 解析其他参数
            if (analysisType != null && !analysisType.trim().isEmpty()) {
                request.setAnalysisType(analysisType);
            }
            
            if (timeGranularity != null && !timeGranularity.trim().isEmpty()) {
                request.setTimeGranularity(timeGranularity);
            }
            
            // 解析ID列表参数
            if (courseIds != null && !courseIds.trim().isEmpty()) {
                request.setCourseIds(java.util.Arrays.stream(courseIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(java.util.stream.Collectors.toList()));
            }
            
            if (examIds != null && !examIds.trim().isEmpty()) {
                request.setExamIds(java.util.Arrays.stream(examIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(java.util.stream.Collectors.toList()));
            }
            
            if (studentIds != null && !studentIds.trim().isEmpty()) {
                request.setStudentIds(java.util.Arrays.stream(studentIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(java.util.stream.Collectors.toList()));
            }
            
            ExamAnalysisResponseDTO analysis = teacherAnalyticsService.getCompleteAnalysis(request);
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            log.error("获取完整分析报告失败", e);
            return ApiResponse.error("获取完整分析报告失败：" + e.getMessage());
        }
    }

    /**
     * 获取快速概览（简化版统计）
     */
    @GetMapping("/quick-overview")
    @RequireTeacher
    public ApiResponse<ExamAnalysisResponseDTO.OverviewStats> getQuickOverview(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        log.info("用户 {} 请求快速概览", UserContext.getCurrentUserId());
        
        try {
            ExamAnalysisRequestDTO request = new ExamAnalysisRequestDTO();
            request.setTeacherId(UserContext.getCurrentUserId());
            
            // 如果没有指定时间范围，默认查询最近30天
            if (startTime == null || endTime == null) {
                request.setStartTime(java.time.LocalDateTime.now().minusDays(30));
                request.setEndTime(java.time.LocalDateTime.now());
            } else {
                request.setStartTime(java.time.LocalDateTime.parse(startTime));
                request.setEndTime(java.time.LocalDateTime.parse(endTime));
            }
            
            ExamAnalysisResponseDTO.OverviewStats stats = teacherAnalyticsService.getOverviewStats(request);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取快速概览失败", e);
            return ApiResponse.error("获取快速概览失败：" + e.getMessage());
        }
    }

    /**
     * 测试接口 - 获取基本统计信息
     */
    @GetMapping("/test")
    @RequireTeacher
    public ApiResponse<String> test() {
        return ApiResponse.success("教师分析功能已成功实现！");
    }
}


