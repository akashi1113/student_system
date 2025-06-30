package com.csu.sms.persistence;

import com.csu.sms.dto.*;
import org.apache.ibatis.annotations.Mapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface GradeAnalysisMapper {

    /**
     * 获取用户考试记录列表 (带分页)
     */
    List<ExamRecordDTO> getUserExamRecordsWithPaging(Long userId,
                                                     LocalDateTime startDate,
                                                     LocalDateTime endDate,
                                                     int offset,
                                                     int pageSize);

    /**
     * 计算用户考试记录总数
     */
    long countUserExamRecords(Long userId,
                              LocalDateTime startDate,
                              LocalDateTime endDate);

    /**
     * 获取用户学习记录列表 (带分页)
     */
    List<StudyRecordDTO> getUserStudyRecordsWithPaging(Long userId,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate,
                                                       int offset,
                                                       int pageSize);

    /**
     * 计算用户学习记录总数
     */
    long countUserStudyRecords(Long userId,
                               LocalDateTime startDate,
                               LocalDateTime endDate);

    /**
     * 根据考试ID获取考试记录 (带分页)
     */
    List<ExamRecordDTO> getExamRecordsByExamWithPaging(Long examId,
                                                       int offset,
                                                       int pageSize);

    /**
     * 计算指定考试的记录总数
     */
    long countExamRecordsByExam(Long examId);

    /**
     * 获取用户考试统计数据
     */
    Map<String, Object> getUserExamStats(Long userId,
                                         LocalDateTime startDate,
                                         LocalDateTime endDate);

    /**
     * 获取用户学习统计数据
     */
    Map<String, Object> getUserStudyStats(Long userId,
                                          LocalDateTime startDate,
                                          LocalDateTime endDate);

    /**
     * 获取用户各课程表现
     */
    List<Map<String, Object>> getUserCoursePerformances(Long userId,
                                                        LocalDateTime startDate,
                                                        LocalDateTime endDate);

    /**
     * 获取用户每日学习数据 (用于折线图)
     */
    List<Map<String, Object>> getUserDailyStudyData(Long userId,
                                                    LocalDateTime startDate,
                                                    LocalDateTime endDate);

    /**
     * 获取用户考试成绩趋势 (用于折线图)
     */
    List<Map<String, Object>> getUserExamTrend(Long userId,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate);
}