package com.csu.sms.service.impl;

import com.csu.sms.dto.analytics.ExamAnalysisRequestDTO;
import com.csu.sms.dto.analytics.ExamAnalysisResponseDTO;
import com.csu.sms.persistence.TeacherAnalyticsMapper;
import com.csu.sms.service.TeacherAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师分析服务实现类
 * @author CSU Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherAnalyticsServiceImpl implements TeacherAnalyticsService {

    private final TeacherAnalyticsMapper teacherAnalyticsMapper;

    @Override
    public ExamAnalysisResponseDTO.OverviewStats getOverviewStats(ExamAnalysisRequestDTO request) {
        log.info("获取总体统计，请求参数：{}", request);
        
        try {
            Map<String, Object> stats = teacherAnalyticsMapper.getOverviewStats(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getCourseIds(),
                    request.getExamIds(),
                    request.getStudentIds()
            );
            
            ExamAnalysisResponseDTO.OverviewStats overviewStats = new ExamAnalysisResponseDTO.OverviewStats();
            
            if (stats != null) {
                overviewStats.setTotalStudents(getLongValue(stats, "totalStudents"));
                overviewStats.setTotalExams(getLongValue(stats, "totalExams"));
                overviewStats.setTotalExamRecords(getLongValue(stats, "totalExamRecords"));
                overviewStats.setAverageScore(getDoubleValue(stats, "averageScore"));
                overviewStats.setPassRate(getDoubleValue(stats, "passRate"));
                overviewStats.setParticipationRate(getDoubleValue(stats, "participationRate"));
                
                // 获取活跃学生数和已完成考试数
                overviewStats.setActiveStudents(teacherAnalyticsMapper.getActiveStudentsCount(
                        request.getStartTime(), request.getEndTime()));
                overviewStats.setCompletedExams(teacherAnalyticsMapper.getCompletedExamsCount(
                        request.getStartTime(), request.getEndTime()));
            }
            
            return overviewStats;
        } catch (Exception e) {
            log.error("获取总体统计失败", e);
            return new ExamAnalysisResponseDTO.OverviewStats();
        }
    }

    @Override
    public ExamAnalysisResponseDTO.ScoreDistribution getScoreDistribution(ExamAnalysisRequestDTO request) {
        log.info("获取成绩分布，请求参数：{}", request);
        
        try {
            Map<String, Object> distribution = teacherAnalyticsMapper.getScoreDistribution(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getCourseIds(),
                    request.getExamIds(),
                    request.getStudentIds()
            );
            
            ExamAnalysisResponseDTO.ScoreDistribution scoreDistribution = new ExamAnalysisResponseDTO.ScoreDistribution();
            
            if (distribution != null) {
                scoreDistribution.setExcellentCount(getLongValue(distribution, "excellentCount"));
                scoreDistribution.setGoodCount(getLongValue(distribution, "goodCount"));
                scoreDistribution.setAverageCount(getLongValue(distribution, "averageCount"));
                scoreDistribution.setPassCount(getLongValue(distribution, "passCount"));
                scoreDistribution.setFailCount(getLongValue(distribution, "failCount"));
                
                // 构建详细分布
                Map<String, Long> detailedDistribution = new HashMap<>();
                detailedDistribution.put("优秀(90-100)", scoreDistribution.getExcellentCount());
                detailedDistribution.put("良好(80-89)", scoreDistribution.getGoodCount());
                detailedDistribution.put("中等(70-79)", scoreDistribution.getAverageCount());
                detailedDistribution.put("及格(60-69)", scoreDistribution.getPassCount());
                detailedDistribution.put("不及格(0-59)", scoreDistribution.getFailCount());
                scoreDistribution.setDistribution(detailedDistribution);
            }
            
            return scoreDistribution;
        } catch (Exception e) {
            log.error("获取成绩分布失败", e);
            return new ExamAnalysisResponseDTO.ScoreDistribution();
        }
    }

    @Override
    public List<ExamAnalysisResponseDTO.CourseComparison> getCourseComparisons(ExamAnalysisRequestDTO request) {
        log.info("获取课程对比，请求参数：{}", request);
        
        try {
            List<Map<String, Object>> comparisons = teacherAnalyticsMapper.getCourseComparisons(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getCourseIds(),
                    request.getExamIds(),
                    request.getStudentIds()
            );
            
            List<ExamAnalysisResponseDTO.CourseComparison> result = new ArrayList<>();
            
            for (Map<String, Object> comparison : comparisons) {
                ExamAnalysisResponseDTO.CourseComparison courseComparison = new ExamAnalysisResponseDTO.CourseComparison();
                courseComparison.setCourseId(getLongValue(comparison, "courseId"));
                courseComparison.setCourseName(getStringValue(comparison, "courseName"));
                courseComparison.setExamCount(getLongValue(comparison, "examCount"));
                courseComparison.setAverageScore(getDoubleValue(comparison, "averageScore"));
                courseComparison.setPassRate(getDoubleValue(comparison, "passRate"));
                courseComparison.setStudentCount(getLongValue(comparison, "studentCount"));
                courseComparison.setDifficultyLevel(getDoubleValue(comparison, "difficultyLevel"));
                result.add(courseComparison);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取课程对比失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExamAnalysisResponseDTO.ExamComparison> getExamComparisons(ExamAnalysisRequestDTO request) {
        log.info("获取考试对比，请求参数：{}", request);
        
        try {
            List<Map<String, Object>> comparisons = teacherAnalyticsMapper.getExamComparisons(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getCourseIds(),
                    request.getExamIds(),
                    request.getStudentIds()
            );
            
            List<ExamAnalysisResponseDTO.ExamComparison> result = new ArrayList<>();
            
            for (Map<String, Object> comparison : comparisons) {
                ExamAnalysisResponseDTO.ExamComparison examComparison = new ExamAnalysisResponseDTO.ExamComparison();
                examComparison.setExamName(getStringValue(comparison, "examName"));
                examComparison.setAverageScore(getDoubleValue(comparison, "averageScore"));
                result.add(examComparison);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取考试对比失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExamAnalysisResponseDTO.TrendData> getTrendAnalysis(ExamAnalysisRequestDTO request) {
        log.info("获取趋势分析，请求参数：{}", request);
        
        try {
            List<Map<String, Object>> trendData = teacherAnalyticsMapper.getTrendData(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getTimeGranularity(),
                    request.getCourseIds(),
                    request.getExamIds(),
                    request.getStudentIds()
            );
            
            List<ExamAnalysisResponseDTO.TrendData> result = new ArrayList<>();
            
            for (Map<String, Object> data : trendData) {
                ExamAnalysisResponseDTO.TrendData trend = new ExamAnalysisResponseDTO.TrendData();
                trend.setTimePoint(getStringValue(data, "timePoint"));
                trend.setAverageScore(getDoubleValue(data, "averageScore"));
                trend.setExamCount(getLongValue(data, "examCount"));
                trend.setStudentCount(getLongValue(data, "studentCount"));
                trend.setPassRate(getDoubleValue(data, "passRate"));
                result.add(trend);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取趋势分析失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExamAnalysisResponseDTO.StudentRanking> getStudentRankings(ExamAnalysisRequestDTO request) {
        log.info("获取学生排名，请求参数：{}", request);
        
        try {
            List<Map<String, Object>> rankings = teacherAnalyticsMapper.getStudentRankings(
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getCourseIds(),
                    request.getExamIds(),
                    request.getStudentIds(),
                    50 // 限制返回前50名
            );
            
            List<ExamAnalysisResponseDTO.StudentRanking> result = new ArrayList<>();
            
            for (Map<String, Object> ranking : rankings) {
                ExamAnalysisResponseDTO.StudentRanking studentRanking = new ExamAnalysisResponseDTO.StudentRanking();
                studentRanking.setStudentId(getLongValue(ranking, "studentId"));
                studentRanking.setStudentName(getStringValue(ranking, "studentName"));
                studentRanking.setAverageScore(getDoubleValue(ranking, "averageScore"));
                studentRanking.setExamCount(getLongValue(ranking, "examCount"));
                studentRanking.setRank(getIntegerValue(ranking, "rank"));
                studentRanking.setImprovement(0.0); // 暂时设为0，后续可以计算进步幅度
                result.add(studentRanking);
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取学生排名失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public ExamAnalysisResponseDTO getCompleteAnalysis(ExamAnalysisRequestDTO request) {
        log.info("获取完整分析报告，请求参数：{}", request);
        
        ExamAnalysisResponseDTO response = new ExamAnalysisResponseDTO();
        
        try {
            response.setOverviewStats(getOverviewStats(request));
            response.setScoreDistribution(getScoreDistribution(request));
            response.setCourseComparisons(getCourseComparisons(request));
            response.setExamComparisons(getExamComparisons(request));
            response.setTrendData(getTrendAnalysis(request));
            response.setStudentRankings(getStudentRankings(request));
            
            return response;
        } catch (Exception e) {
            log.error("获取完整分析报告失败", e);
            return response;
        }
    }

    // 工具方法：安全获取Long值
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    // 工具方法：安全获取Double值
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    // 工具方法：安全获取String值
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    // 工具方法：安全获取Integer值
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
} 