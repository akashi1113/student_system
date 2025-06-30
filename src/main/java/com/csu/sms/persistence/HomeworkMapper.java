package com.csu.sms.persistence;

import com.csu.sms.model.homework.Homework;
import com.csu.sms.model.homework.HomeworkQuestion;
import com.csu.sms.model.homework.HomeworkSubmission;
import com.csu.sms.model.homework.HomeworkAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface HomeworkMapper {

    // ================ 作业管理 ================
    int insertHomework(Homework homework);
    int updateHomework(Homework homework);
    int deleteHomework(Long id);
    Homework selectHomeworkById(Long id);
    List<Homework> selectHomeworkByTeacherId(Long teacherId);
    List<Homework> selectHomeworkByCourseId(Long courseId);
    List<Homework> selectAvailableHomeworkByStudentId(Long studentId);
    List<Homework> selectHomeworkByStudentId(Long studentId);
    int updateHomeworkStatus(@Param("id") Long id, @Param("status") String status);

    // ================ 课程相关 ================
    List<Long> selectStudentIdsByCourseId(Long courseId);
    List<Map<String, Object>> selectCoursesByTeacherId(Long teacherId);
    List<Map<String, Object>> selectCoursesByStudentId(Long studentId);
    boolean checkStudentCourseAccess(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    boolean checkTeacherCourseAccess(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);

    // ================ 作业题目管理 ================
    int insertHomeworkQuestion(HomeworkQuestion question);
    int batchInsertHomeworkQuestions(List<HomeworkQuestion> questions);
    int updateHomeworkQuestion(HomeworkQuestion question);
    int deleteHomeworkQuestion(Long id);
    int deleteHomeworkQuestionsByHomeworkId(Long homeworkId);
    HomeworkQuestion selectHomeworkQuestionById(Long id);
    List<HomeworkQuestion> selectHomeworkQuestionsByHomeworkId(Long homeworkId);

    // ================ 作业提交管理 ================
    int insertHomeworkSubmission(HomeworkSubmission submission);
    int updateHomeworkSubmission(HomeworkSubmission submission);
    int updateHomeworkGrade(HomeworkSubmission submission);
    HomeworkSubmission selectHomeworkSubmissionById(Long id);
    List<HomeworkSubmission> selectHomeworkSubmissionsByHomeworkIdAndStudentId(@Param("homeworkId") Long homeworkId,
                                                                               @Param("studentId") Long studentId);
    List<HomeworkSubmission> selectHomeworkSubmissionsByHomeworkId(Long homeworkId);
    List<HomeworkSubmission> selectHomeworkSubmissionsByStudentId(Long studentId);
    List<HomeworkSubmission> selectHomeworkSubmissionsByCourseId(Long courseId);
    int countSubmissionsByHomeworkIdAndStudentId(@Param("homeworkId") Long homeworkId,
                                                 @Param("studentId") Long studentId);

    // ================ 作业答案管理 ================
    int insertHomeworkAnswer(HomeworkAnswer answer);
    int batchInsertHomeworkAnswers(List<HomeworkAnswer> answers);
    int updateHomeworkAnswer(HomeworkAnswer answer);
    int deleteHomeworkAnswersBySubmissionId(Long submissionId);
    List<HomeworkAnswer> selectHomeworkAnswersBySubmissionId(Long submissionId);
    int updateHomeworkAnswerScore(HomeworkAnswer answer);

    // ================ 统计查询 ================
    Map<String, Object> selectHomeworkStatistics(Long homeworkId);
    List<Map<String, Object>> selectStudentHomeworkProgress(Long studentId);
    Map<String, Object> selectCourseHomeworkStatistics(Long courseId);
}