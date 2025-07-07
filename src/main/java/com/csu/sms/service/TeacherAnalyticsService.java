package com.csu.sms.service;

import com.csu.sms.dto.analytics.ExamAnalysisRequestDTO;
import com.csu.sms.dto.analytics.ExamAnalysisResponseDTO;

/**
 * 教师分析服务接口
 * @author CSU Team
 */
public interface TeacherAnalyticsService {
    
    /**
     * 获取总体概览统计
     * @param request 分析请求
     * @return 总体统计
     */
    ExamAnalysisResponseDTO.OverviewStats getOverviewStats(ExamAnalysisRequestDTO request);
    
    /**
     * 获取成绩分布分析
     * @param request 分析请求
     * @return 成绩分布
     */
    ExamAnalysisResponseDTO.ScoreDistribution getScoreDistribution(ExamAnalysisRequestDTO request);
    
    /**
     * 获取课程对比分析
     * @param request 分析请求
     * @return 课程对比列表
     */
    java.util.List<ExamAnalysisResponseDTO.CourseComparison> getCourseComparisons(ExamAnalysisRequestDTO request);
    
    /**
     * 获取考试对比分析
     * @param request 分析请求
     * @return 考试对比列表
     */
    java.util.List<ExamAnalysisResponseDTO.ExamComparison> getExamComparisons(ExamAnalysisRequestDTO request);
    
    /**
     * 获取趋势分析
     * @param request 分析请求
     * @return 趋势数据列表
     */
    java.util.List<ExamAnalysisResponseDTO.TrendData> getTrendAnalysis(ExamAnalysisRequestDTO request);
    
    /**
     * 获取学生排名
     * @param request 分析请求
     * @return 学生排名列表
     */
    java.util.List<ExamAnalysisResponseDTO.StudentRanking> getStudentRankings(ExamAnalysisRequestDTO request);
    
    /**
     * 获取完整分析报告
     * @param request 分析请求
     * @return 完整分析报告
     */
    ExamAnalysisResponseDTO getCompleteAnalysis(ExamAnalysisRequestDTO request);
} 