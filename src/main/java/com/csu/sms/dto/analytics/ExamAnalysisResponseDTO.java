package com.csu.sms.dto.analytics;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 考试分析响应DTO
 * @author CSU Team
 */
@Data
public class ExamAnalysisResponseDTO {
    
    /**
     * 总体统计
     */
    private OverviewStats overviewStats;
    
    /**
     * 成绩分布
     */
    private ScoreDistribution scoreDistribution;
    
    /**
     * 课程对比
     */
    private List<CourseComparison> courseComparisons;
    
    /**
     * 考试对比
     */
    private List<ExamComparison> examComparisons;
    
    /**
     * 趋势数据
     */
    private List<TrendData> trendData;
    
    /**
     * 学生排名
     */
    private List<StudentRanking> studentRankings;
    
    /**
     * 总体统计
     */
    @Data
    public static class OverviewStats {
        private Long totalStudents;           // 学生总数
        private Long totalExams;              // 考试总数
        private Long totalExamRecords;        // 考试记录总数
        private Double averageScore;          // 平均分
        private Double passRate;              // 及格率
        private Double participationRate;     // 参与率
        private Long activeStudents;          // 活跃学生数
        private Long completedExams;          // 已完成考试数
    }
    
    /**
     * 成绩分布
     */
    @Data
    public static class ScoreDistribution {
        private Long excellentCount;          // 优秀人数 (90-100)
        private Long goodCount;               // 良好人数 (80-89)
        private Long averageCount;            // 中等人数 (70-79)
        private Long passCount;               // 及格人数 (60-69)
        private Long failCount;               // 不及格人数 (0-59)
        private Map<String, Long> distribution; // 详细分布
    }
    
    /**
     * 课程对比
     */
    @Data
    public static class CourseComparison {
        private Long courseId;
        private String courseName;
        private Long examCount;
        private Double averageScore;
        private Double passRate;
        private Long studentCount;
        private Double difficultyLevel;       // 难度等级 (0-1)
    }
    
    /**
     * 考试对比
     */
    @Data
    public static class ExamComparison {
        private String examName;               // 考试名称
        private Double averageScore;           // 平均分
    }
    
    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        private String timePoint;             // 时间点
        private Double averageScore;          // 平均分
        private Long examCount;               // 考试数量
        private Long studentCount;            // 学生数量
        private Double passRate;              // 及格率
    }
    
    /**
     * 学生排名
     */
    @Data
    public static class StudentRanking {
        private Long studentId;
        private String studentName;
        private Double averageScore;
        private Long examCount;
        private Integer rank;
        private Double improvement;           // 进步幅度
    }
} 