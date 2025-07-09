package com.csu.sms.persistence;

import com.csu.sms.model.ExamMonitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExamMonitorMapper {

    int insert(ExamMonitor examMonitor);

    int update(ExamMonitor examMonitor);

    int deleteById(Long id);

    ExamMonitor selectById(Long id);

    List<ExamMonitor> selectByExamId(@Param("examId") Long examId);

    List<ExamMonitor> selectByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    List<ExamMonitor> selectAbnormalRecords(@Param("examId") Long examId, @Param("userId") Long userId);

    int countByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    int countAbnormalByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    List<ExamMonitor> selectByTimeRange(@Param("examId") Long examId, @Param("userId") Long userId,
                                        @Param("startTime") Long startTime, @Param("endTime") Long endTime);
}