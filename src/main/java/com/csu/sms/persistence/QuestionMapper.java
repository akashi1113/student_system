package com.csu.sms.persistence;

import com.csu.sms.model.question.AnswerRecord;
import com.csu.sms.model.question.AnswerStatistics;
import com.csu.sms.model.question.Question;
import com.csu.sms.model.question.QuestionOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionMapper {

    // ========== 题目相关操作 ==========

    // 根据考试ID查询所有题目
    List<Question> findByExamId(@Param("examId") Long examId);

    // 根据题目ID查询题目
    Question findById(@Param("id") Long id);

    // 根据题目类型查询题目
    List<Question> findByType(@Param("type") String type);

    // 根据难度查询题目
    List<Question> findByDifficulty(@Param("difficulty") String difficulty);

    // 插入题目
    int insert(Question question);

    // 批量插入题目
    int batchInsert(@Param("questions") List<Question> questions);

    // 更新题目
    int update(Question question);

    // 删除题目
    int deleteById(@Param("id") Long id);

    // 根据考试ID删除所有题目
    int deleteByExamId(@Param("examId") Long examId);

    // 批量删除题目
    int batchDelete(@Param("ids") List<Long> ids);

    // 获取考试的题目总数
    int countByExamId(@Param("examId") Long examId);

    // 获取考试的总分
    Integer getTotalScoreByExamId(@Param("examId") Long examId);

    // 获取题目的最大排序号
    Integer getMaxOrderNumByExamId(@Param("examId") Long examId);

    // 更新题目排序
    int updateOrderNum(@Param("id") Long id, @Param("orderNum") Integer orderNum);

    // ========== 题目选项相关操作 ==========

    // 根据题目ID查询所有选项
    List<QuestionOption> findOptionsByQuestionId(@Param("questionId") Long questionId);

    // 根据题目ID列表批量查询选项
    List<QuestionOption> findOptionsByQuestionIds(@Param("questionIds") List<Long> questionIds);

    // 插入题目选项
    int insertOption(QuestionOption option);

    // 批量插入题目选项
    int batchInsertOptions(@Param("options") List<QuestionOption> options);

    // 更新题目选项
    int updateOption(QuestionOption option);

    // 删除题目选项
    int deleteOptionById(@Param("id") Long id);

    // 根据题目ID删除所有选项
    int deleteOptionsByQuestionId(@Param("questionId") Long questionId);

    // 根据题目ID列表批量删除选项
    int deleteOptionsByQuestionIds(@Param("questionIds") List<Long> questionIds);

    // 获取题目的正确答案
    String getCorrectAnswersByQuestionId(@Param("questionId") Long questionId);

    // 获取单选题或判断题的正确选项ID
    Long getSingleCorrectOptionId(@Param("questionId") Long questionId);

    // 获取多选题的正确选项ID列表
    List<Long> getMultipleCorrectOptionIds(@Param("questionId") Long questionId);

    // ========== 答题记录相关操作 ==========

    // 插入答题记录
    int insertAnswerRecord(AnswerRecord answerRecord);

    // 批量插入答题记录
    int batchInsertAnswerRecords(@Param("records") List<AnswerRecord> records);

    // 根据考试记录ID查询答题记录
    List<AnswerRecord> findAnswerRecordsByExamRecordId(@Param("examRecordId") Long examRecordId);

    // 根据考试记录ID和题目ID查询答题记录
    AnswerRecord findAnswerRecord(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId);

    // 更新答题记录
    int updateAnswerRecord(AnswerRecord answerRecord);

    // 删除答题记录
    int deleteAnswerRecord(@Param("id") Long id);

    // 根据考试记录ID删除所有答题记录
    int deleteAnswerRecordsByExamRecordId(@Param("examRecordId") Long examRecordId);

    // 计算考试记录的总分
    Integer calculateTotalScore(@Param("examRecordId") Long examRecordId);

    // 计算某学生考试记录的总分
    Map<String, Object> calculateDetailedScore(@Param("examRecordId") Long examRecordId);

    // 统计答对的题目数量
    int countCorrectAnswers(@Param("examRecordId") Long examRecordId);

    // 统计答题记录
    AnswerStatistics getAnswerStatistics(@Param("examRecordId") Long examRecordId);
}