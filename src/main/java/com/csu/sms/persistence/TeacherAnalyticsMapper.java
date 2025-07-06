package com.csu.sms.persistence;

import com.csu.sms.dto.analytics.ExamAnalysisResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 教师分析数据访问层
 * @author CSU Team
 */
@Mapper
public interface TeacherAnalyticsMapper {
    
    /**
     * 获取总体统计
     */
    Map<String, Object> getOverviewStats(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("courseIds") List<Long> courseIds,
            @Param("examIds") List<Long> examIds,
            @Param("studentIds") List<Long> studentIds
    );
    
    /**
     * 获取成绩分布
     */
    Map<String, Object> getScoreDistribution(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("courseIds") List<Long> courseIds,
            @Param("examIds") List<Long> examIds,
            @Param("studentIds") List<Long> studentIds
    );
    
    /**
     * 获取课程对比数据
     */
    List<Map<String, Object>> getCourseComparisons(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("courseIds") List<Long> courseIds,
            @Param("examIds") List<Long> examIds,
            @Param("studentIds") List<Long> studentIds
    );
    
    /**
     * 获取趋势数据
     */
    List<Map<String, Object>> getTrendData(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("timeGranularity") String timeGranularity,
            @Param("courseIds") List<Long> courseIds,
            @Param("examIds") List<Long> examIds,
            @Param("studentIds") List<Long> studentIds
    );
    
    /**
     * 获取学生排名
     */
    List<Map<String, Object>> getStudentRankings(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("courseIds") List<Long> courseIds,
            @Param("examIds") List<Long> examIds,
            @Param("studentIds") List<Long> studentIds,
            @Param("limit") Integer limit
    );
    
    /**
     * 获取活跃学生数
     */
    Long getActiveStudentsCount(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 获取已完成考试数
     */
    Long getCompletedExamsCount(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    /**
     * 获取考试对比数据
     */
    List<Map<String, Object>> getExamComparisons(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("courseIds") List<Long> courseIds,
            @Param("examIds") List<Long> examIds,
            @Param("studentIds") List<Long> studentIds
    );
} 