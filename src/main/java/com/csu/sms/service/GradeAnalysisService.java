package com.csu.sms.service;

import com.csu.sms.dto.*;
import com.csu.sms.persistence.GradeAnalysisMapper;
import com.csu.sms.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GradeAnalysisService {
    @Autowired
    private GradeAnalysisMapper gradeAnalysisMapper;

    public PageResult<ExamRecordDTO> getUserExamRecords(Long userId, int pageNum, int pageSize,
                                                        LocalDateTime startDate, LocalDateTime endDate) {
        int offset = (pageNum - 1) * pageSize;
        long total = gradeAnalysisMapper.countUserExamRecords(userId, startDate, endDate);
        List<ExamRecordDTO> records = gradeAnalysisMapper.getUserExamRecordsWithPaging(
                userId, startDate, endDate, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public PageResult<StudyRecordDTO> getUserStudyRecords(Long userId, int pageNum, int pageSize,
                                                          LocalDateTime startDate, LocalDateTime endDate) {
        int offset = (pageNum - 1) * pageSize;
        long total = gradeAnalysisMapper.countUserStudyRecords(userId, startDate, endDate);
        List<StudyRecordDTO> records = gradeAnalysisMapper.getUserStudyRecordsWithPaging(
                userId, startDate, endDate, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public PageResult<ExamRecordDTO> getExamRecordsByExam(Long examId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        long total = gradeAnalysisMapper.countExamRecordsByExam(examId);
        List<ExamRecordDTO> records = gradeAnalysisMapper.getExamRecordsByExamWithPaging(examId, offset, pageSize);
        return new PageResult<>(records, total, pageNum, pageSize);
    }

    public GradeAnalysisDTO getUserAnalysis(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        GradeAnalysisDTO analysis = new GradeAnalysisDTO();
        analysis.setUserId(userId);
        analysis.setStartDate(startDate);
        analysis.setEndDate(endDate);
        Map<String, Object> examStats = gradeAnalysisMapper.getUserExamStats(userId, startDate, endDate);
        if (examStats != null) {
            Object avgScore = examStats.get("averageScore");
            if (avgScore != null) {
                analysis.setAverageScore(new BigDecimal(avgScore.toString()));
            }
            Object maxScore = examStats.get("maxScore");
            if (maxScore != null) {
                analysis.setMaxScore(new BigDecimal(maxScore.toString()));
            }
            Object minScore = examStats.get("minScore");
            if (minScore != null) {
                analysis.setMinScore(new BigDecimal(minScore.toString()));
            }
            Object totalExamsObj = examStats.get("totalExams");
            analysis.setTotalExams(totalExamsObj != null ? ((Number) totalExamsObj).intValue() : 0);
            Object passedExamsObj = examStats.get("passedExams");
            analysis.setPassedExams(passedExamsObj != null ? ((Number) passedExamsObj).intValue() : 0);
            if (analysis.getTotalExams() > 0) {
                BigDecimal passRate = new BigDecimal(analysis.getPassedExams())
                        .divide(new BigDecimal(analysis.getTotalExams()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                analysis.setPassRate(passRate);
            }
        }
        Map<String, Object> studyStats = gradeAnalysisMapper.getUserStudyStats(userId, startDate, endDate);
        if (studyStats != null) {
            Object totalStudyDurationObj = studyStats.get("totalStudyDuration");
            analysis.setTotalStudyDuration(totalStudyDurationObj != null ? ((Number) totalStudyDurationObj).longValue() : 0L);
            Object totalStudyDaysObj = studyStats.get("totalStudyDays");
            analysis.setTotalStudyDays(totalStudyDaysObj != null ? ((Number) totalStudyDaysObj).intValue() : 0);
            Object averageDailyDurationObj = studyStats.get("averageDailyDuration");
            analysis.setAverageDailyDuration(averageDailyDurationObj != null ? ((Number) averageDailyDurationObj).longValue() : 0L);
            Object totalVideosObj = studyStats.get("totalVideos");
            analysis.setTotalVideos(totalVideosObj != null ? ((Number) totalVideosObj).intValue() : 0);
            Object completedVideosObj = studyStats.get("completedVideos");
            analysis.setCompletedVideos(completedVideosObj != null ? ((Number) completedVideosObj).intValue() : 0);
            if (analysis.getTotalVideos() > 0) {
                BigDecimal completionRate = new BigDecimal(analysis.getCompletedVideos())
                        .divide(new BigDecimal(analysis.getTotalVideos()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                analysis.setCompletionRate(completionRate);
            }
        }
        List<Map<String, Object>> coursePerfs = gradeAnalysisMapper.getUserCoursePerformances(userId, startDate, endDate);
        List<CoursePerformanceDTO> coursePerformances = new ArrayList<>();
        for (Map<String, Object> perf : coursePerfs) {
            CoursePerformanceDTO dto = new CoursePerformanceDTO();
            Object courseIdObj = perf.get("courseId");
            dto.setCourseId(courseIdObj != null ? ((Number) courseIdObj).longValue() : 0L);
            dto.setCourseTitle((String) perf.get("courseTitle"));
            Object avgScore = perf.get("averageScore");
            if (avgScore != null) {
                dto.setAverageScore(new BigDecimal(avgScore.toString()));
            }
            Object examCountObj = perf.get("examCount");
            dto.setExamCount(examCountObj != null ? ((Number) examCountObj).intValue() : 0);
            Object studyDurationObj = perf.get("studyDuration");
            dto.setStudyDuration(studyDurationObj != null ? ((Number) studyDurationObj).longValue() : 0L);
            Object videoCountObj = perf.get("videoCount");
            dto.setVideoCount(videoCountObj != null ? ((Number) videoCountObj).intValue() : 0);
            Object completedVideosPerfObj = perf.get("completedVideos");
            dto.setCompletedVideos(completedVideosPerfObj != null ? ((Number) completedVideosPerfObj).intValue() : 0);
            if (dto.getVideoCount() > 0) {
                BigDecimal completionRate = new BigDecimal(dto.getCompletedVideos())
                        .divide(new BigDecimal(dto.getVideoCount()), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100));
                dto.setCompletionRate(completionRate);
            }
            coursePerformances.add(dto);
        }
        analysis.setCoursePerformances(coursePerformances);
        calculatePerformanceEvaluation(analysis);
        return analysis;
    }

    public Map<String, Object> getChartData(Long userId, String chartType,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        String type = chartType.toLowerCase();
        if ("radar".equals(type)) {
            return generateRadarChartData(userId, startDate, endDate);
        } else if ("bar".equals(type)) {
            return generateBarChartData(userId, startDate, endDate);
        } else if ("line".equals(type)) {
            return generateLineChartData(userId, startDate, endDate);
        } else if ("pie".equals(type)) {
            return generatePieChartData(userId, startDate, endDate);
        } else if ("score-trend".equals(type)) {
            return generateScoreTrendData(userId, startDate, endDate);
        } else {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "未支持的图表类型: " + chartType);
            return error;
        }
    }

    private void calculatePerformanceEvaluation(GradeAnalysisDTO analysis) {
        BigDecimal avgScore = Optional.ofNullable(analysis.getAverageScore()).orElse(BigDecimal.ZERO);
        BigDecimal passRate = Optional.ofNullable(analysis.getPassRate()).orElse(BigDecimal.ZERO);
        BigDecimal completionRate = Optional.ofNullable(analysis.getCompletionRate()).orElse(BigDecimal.ZERO);
        Long totalDuration = Optional.ofNullable(analysis.getTotalStudyDuration()).orElse(0L);
        BigDecimal efficiencyScore = calculateEfficiencyScore(avgScore, passRate, completionRate, totalDuration);
        analysis.setEfficiencyScore(efficiencyScore);
        String performanceLevel = determinePerformanceLevel(avgScore, passRate, completionRate, efficiencyScore);
        analysis.setPerformanceLevel(performanceLevel);
        List<String> suggestions = generateSuggestions(avgScore, passRate, completionRate, totalDuration);
        analysis.setSuggestions(suggestions);
    }

    private BigDecimal calculateEfficiencyScore(BigDecimal avgScore, BigDecimal passRate,
                                                BigDecimal completionRate, Long totalDuration) {
        BigDecimal scoreComponent = avgScore.multiply(new BigDecimal("0.4"));
        BigDecimal passComponent = passRate.multiply(new BigDecimal("0.3"));
        BigDecimal completionComponent = completionRate.multiply(new BigDecimal("0.2"));
        BigDecimal durationEfficiency = calculateDurationEfficiency(totalDuration);
        BigDecimal durationComponent = durationEfficiency.multiply(new BigDecimal("0.1"));
        return scoreComponent.add(passComponent).add(completionComponent).add(durationComponent);
    }

    private BigDecimal calculateDurationEfficiency(Long totalDuration) {
        if (totalDuration == null || totalDuration == 0) return BigDecimal.ZERO;
        double hours = totalDuration / 3600.0;
        if (hours >= 10 && hours <= 50) {
            return new BigDecimal("100");
        } else if (hours < 10) {
            return new BigDecimal(hours * 10);
        } else {
            return new BigDecimal(Math.max(0, 100 - (hours - 50) * 2));
        }
    }

    private String determinePerformanceLevel(BigDecimal avgScore, BigDecimal passRate,
                                             BigDecimal completionRate, BigDecimal efficiencyScore) {
        BigDecimal combinedScore = avgScore.multiply(new BigDecimal("0.4"))
                .add(passRate.multiply(new BigDecimal("0.3")))
                .add(completionRate.multiply(new BigDecimal("0.2")))
                .add(efficiencyScore.multiply(new BigDecimal("0.1")));
        if (combinedScore.compareTo(new BigDecimal("90")) >= 0) {
            return "优秀";
        } else if (combinedScore.compareTo(new BigDecimal("80")) >= 0) {
            return "良好";
        } else if (combinedScore.compareTo(new BigDecimal("70")) >= 0) {
            return "一般";
        } else {
            return "较差";
        }
    }

    private List<String> generateSuggestions(BigDecimal avgScore, BigDecimal passRate,
                                             BigDecimal completionRate, Long totalDuration) {
        List<String> suggestions = new ArrayList<>();
        if (avgScore.compareTo(new BigDecimal("80")) < 0) {
            suggestions.add("建议加强知识点复习，提高考试成绩");
        }
        if (passRate.compareTo(new BigDecimal("80")) < 0) {
            suggestions.add("考试通过率偏低，建议在充分准备后再参加考试");
        }
        if (completionRate.compareTo(new BigDecimal("80")) < 0) {
            suggestions.add("视频完成率不高，建议按计划完成所有学习视频");
        }
        if (totalDuration < 3600) {
            suggestions.add("学习时间较少，建议增加学习投入");
        } else if (totalDuration > 180000) {
            suggestions.add("学习时间较长，建议提高学习效率，注意劳逸结合");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("学习表现良好，请继续保持");
        }
        return suggestions;
    }

    private Map<String, Object> generateRadarChartData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        GradeAnalysisDTO analysis = getUserAnalysis(userId, startDate, endDate);
        List<String> indicators = Arrays.asList("考试成绩", "通过率", "完成率", "学习时长", "学习效率");
        List<Integer> values = Arrays.asList(
                Optional.ofNullable(analysis.getAverageScore()).orElse(BigDecimal.ZERO).intValue(),
                Optional.ofNullable(analysis.getPassRate()).orElse(BigDecimal.ZERO).intValue(),
                Optional.ofNullable(analysis.getCompletionRate()).orElse(BigDecimal.ZERO).intValue(),
                calculateDurationScore(analysis.getTotalStudyDuration()),
                Optional.ofNullable(analysis.getEfficiencyScore()).orElse(BigDecimal.ZERO).intValue()
        );
        Map<String, Object> radarMap = new java.util.HashMap<>();
        radarMap.put("indicators", indicators);
        radarMap.put("values", values);
        return radarMap;
    }

    private Map<String, Object> generateBarChartData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> coursePerfs = gradeAnalysisMapper.getUserCoursePerformances(userId, startDate, endDate);
        List<String> courseNames = new ArrayList<>();
        List<BigDecimal> avgScores = new ArrayList<>();
        List<Integer> examCounts = new ArrayList<>();
        List<Long> studyDurations = new ArrayList<>();
        for (Map<String, Object> perf : coursePerfs) {
            Object courseTitleObj = perf.get("courseTitle");
            courseNames.add(courseTitleObj != null ? (String) courseTitleObj : "未知课程");
            Object avgScore = perf.get("averageScore");
            avgScores.add(avgScore != null ? new BigDecimal(avgScore.toString()) : BigDecimal.ZERO);
            Object examCountObj = perf.get("examCount");
            examCounts.add(examCountObj != null ? ((Number) examCountObj).intValue() : 0);
            Object studyDurationObj = perf.get("studyDuration");
            studyDurations.add(studyDurationObj != null ? ((Number) studyDurationObj).longValue() : 0L);
        }
        Map<String, Object> barMap = new java.util.HashMap<>();
        barMap.put("courseNames", courseNames);
        barMap.put("avgScores", avgScores);
        barMap.put("examCounts", examCounts);
        barMap.put("studyDurations", studyDurations);
        return barMap;
    }

    private Map<String, Object> generateLineChartData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> dailyData = gradeAnalysisMapper.getUserDailyStudyData(userId, startDate, endDate);
        List<String> dates = new ArrayList<>();
        List<Long> durations = new ArrayList<>();
        List<Integer> videoCounts = new ArrayList<>();
        List<Integer> completedVideos = new ArrayList<>();
        for (Map<String, Object> daily : dailyData) {
            Object studyDateObj = daily.get("studyDate");
            dates.add(studyDateObj != null ? studyDateObj.toString() : "");
            Object totalDurationObj = daily.get("totalDuration");
            durations.add(totalDurationObj != null ? ((Number) totalDurationObj).longValue() : 0L);
            Object videoCountObj = daily.get("videoCount");
            videoCounts.add(videoCountObj != null ? ((Number) videoCountObj).intValue() : 0);
            Object completedVideosObj = daily.get("completedVideos");
            completedVideos.add(completedVideosObj != null ? ((Number) completedVideosObj).intValue() : 0);
        }
        Map<String, Object> lineMap = new java.util.HashMap<>();
        lineMap.put("dates", dates);
        lineMap.put("durations", durations);
        lineMap.put("videoCounts", videoCounts);
        lineMap.put("completedVideos", completedVideos);
        return lineMap;
    }

    private Map<String, Object> generatePieChartData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> coursePerfs = gradeAnalysisMapper.getUserCoursePerformances(userId, startDate, endDate);
        List<Map<String, Object>> pieData = coursePerfs.stream()
                .map(perf -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    Object courseTitleObj = perf.get("courseTitle");
                    map.put("name", courseTitleObj != null ? courseTitleObj : "未知课程");
                    Object studyDurationObj = perf.get("studyDuration");
                    long duration = studyDurationObj != null ? ((Number) studyDurationObj).longValue() : 0L;
                    map.put("value", duration / 60);
                    return map;
                })
                .collect(Collectors.toList());
        Map<String, Object> pieMap = new java.util.HashMap<>();
        pieMap.put("data", pieData);
        return pieMap;
    }

    private Map<String, Object> generateScoreTrendData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> trendData = gradeAnalysisMapper.getUserExamTrend(userId, startDate, endDate);
        List<String> dates = new ArrayList<>();
        List<BigDecimal> scores = new ArrayList<>();
        List<Integer> examCounts = new ArrayList<>();
        for (Map<String, Object> trend : trendData) {
            Object examDateObj = trend.get("examDate");
            dates.add(examDateObj != null ? examDateObj.toString() : "");
            Object avgScore = trend.get("avgScore");
            scores.add(avgScore != null ? new BigDecimal(avgScore.toString()) : BigDecimal.ZERO);
            Object examCountObj = trend.get("examCount");
            examCounts.add(examCountObj != null ? ((Number) examCountObj).intValue() : 0);
        }
        Map<String, Object> scoreTrendMap = new java.util.HashMap<>();
        scoreTrendMap.put("dates", dates);
        scoreTrendMap.put("scores", scores);
        scoreTrendMap.put("examCounts", examCounts);
        return scoreTrendMap;
    }

    private Integer calculateDurationScore(Long totalDuration) {
        if (totalDuration == null || totalDuration == 0) return 0;
        long hours = totalDuration / 3600;
        if (hours >= 50) return 100;
        if (hours >= 30) return 90;
        if (hours >= 20) return 80;
        if (hours >= 10) return 70;
        if (hours >= 5) return 60;
        if (hours >= 1) return 40;
        return 20;
    }
}