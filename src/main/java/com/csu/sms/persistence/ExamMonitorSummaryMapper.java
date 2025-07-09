package com.csu.sms.persistence;

import com.csu.sms.model.ExamMonitorSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ExamMonitorSummaryMapper {

    int insert(ExamMonitorSummary summary);

    int update(ExamMonitorSummary summary);

    int deleteById(Long id);

    ExamMonitorSummary selectById(Long id);

    ExamMonitorSummary selectByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);

    List<ExamMonitorSummary> selectByExamId(@Param("examId") Long examId);

    int upsert(ExamMonitorSummary summary);
}