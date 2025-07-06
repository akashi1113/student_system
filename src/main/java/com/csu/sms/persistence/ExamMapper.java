package com.csu.sms.persistence;

import com.csu.sms.dto.exam.AnswerDTO;
import com.csu.sms.model.booking.ExamTimeSlot;
import com.csu.sms.model.exam.Exam;
import com.csu.sms.model.exam.ExamRecord;
import com.csu.sms.model.exam.ExamStatistics;
import com.csu.sms.model.question.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamMapper {

    // ========== 考试相关操作 ==========

    // 查询所有可用的考试
    List<Exam> findAvailableExams();

    // 查询可预约考试
    List<Exam> findBookableExams();

    // 根据ID查询考试
    Exam findById(@Param("id") Long id);

    // 根据ID列表查询考试
    List<Exam> findByIds(@Param("ids") List<Long> ids);

    // 根据状态查询考试列表
    List<Exam> findByStatus(@Param("status") String status);

    // 根据创建者查询考试列表
    List<Exam> findByCreatedBy(@Param("createdBy") Long createdBy);

    // 插入考试
    int insert(Exam exam);

    // 更新考试
    int update(Exam exam);

    // 删除考试
    int deleteById(@Param("id") Long id);

    // 根据考试ID查询题目列表
    List<Question> findQuestionsByExamId(@Param("examId") Long examId);

    // 统计考试数量
    int countExams();

    // 分页查询考试
    List<Exam> findExamsWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    // ========== 考试记录相关操作 ==========

    // 查询考试记录
    ExamRecord findExamRecord(@Param("examId") Long examId, @Param("userId") Long userId);

    // 根据ID查询考试记录
    ExamRecord findExamRecordById(@Param("id") Long id);

    // 根据用户ID查询考试记录列表
    List<ExamRecord> findExamRecordsByUserId(@Param("userId") Long userId);

    // 根据考试ID查询所有考试记录
    List<ExamRecord> findExamRecordsByExamId(@Param("examId") Long examId);

    // 插入考试记录
    int insertExamRecord(ExamRecord examRecord);

    // 更新考试记录
    int updateExamRecord(ExamRecord examRecord);

    // 删除考试记录
    int deleteExamRecord(@Param("id") Long id);

    // 根据考试ID删除所有考试记录
    int deleteExamRecordsByExamId(@Param("examId") Long examId);

    // 统计考试参与人数
    int countParticipants(@Param("examId") Long examId);

    // 查询考试统计信息
    ExamStatistics getExamStatistics(@Param("examId") Long examId);

    // ========== 答题记录相关操作 ==========

    // 插入答题记录
    int insertAnswerRecord(@Param("examRecordId") Long examRecordId, @Param("answer") AnswerDTO answer);

    // 批量插入答题记录
    int batchInsertAnswerRecords(@Param("examRecordId") Long examRecordId, @Param("answers") List<AnswerDTO> answers);

    // 查询答题记录
    List<AnswerDTO> findAnswerRecords(@Param("examRecordId") Long examRecordId);

    // 根据状态查询考试记录
    List<ExamRecord> findExamRecordsByStatus(@Param("status") String status);

    // 查询学生成绩较低的考试记录
    List<ExamRecord> findLowScoreExamsByUserId(@Param("userId") Long userId, @Param("threshold") Double threshold);

}