package com.csu.sms.service;

import com.csu.sms.model.homework.Homework;
import com.csu.sms.model.homework.HomeworkQuestion;
import com.csu.sms.model.homework.HomeworkSubmission;
import com.csu.sms.model.homework.HomeworkAnswer;
import com.csu.sms.persistence.HomeworkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HomeworkService {

    @Autowired
    private HomeworkMapper homeworkMapper;

    // ================ 作业管理 ================

    /**
     * 创建作业（基于课程）
     */
    public Long createHomework(Homework homework, List<HomeworkQuestion> questions) {
        // 验证教师是否有权限在该课程下创建作业
        if (!homeworkMapper.checkTeacherCourseAccess(homework.getTeacherId(), homework.getCourseId())) {
            throw new RuntimeException("教师无权限在该课程下创建作业");
        }

        // 插入作业
        homeworkMapper.insertHomework(homework);
        Long homeworkId = homework.getId();

        // 插入题目
        if (questions != null && !questions.isEmpty()) {
            for (int i = 0; i < questions.size(); i++) {
                HomeworkQuestion question = questions.get(i);
                question.setHomeworkId(homeworkId);
                question.setQuestionOrder(i + 1);
            }
            homeworkMapper.batchInsertHomeworkQuestions(questions);
        }

        return homeworkId;
    }

    /**
     * 更新作业
     */
    public void updateHomework(Homework homework, List<HomeworkQuestion> questions) {
        // 更新作业信息
        homeworkMapper.updateHomework(homework);

        // 删除原有题目，重新插入
        homeworkMapper.deleteHomeworkQuestionsByHomeworkId(homework.getId());
        if (questions != null && !questions.isEmpty()) {
            for (int i = 0; i < questions.size(); i++) {
                HomeworkQuestion question = questions.get(i);
                question.setHomeworkId(homework.getId());
                question.setQuestionOrder(i + 1);
            }
            homeworkMapper.batchInsertHomeworkQuestions(questions);
        }
    }

    /**
     * 删除作业
     */
    public void deleteHomework(Long id) {
        homeworkMapper.deleteHomework(id);
    }

    /**
     * 根据ID获取作业详情
     */
    @Transactional(readOnly = true)
    public Homework getHomeworkById(Long id) {
        Homework homework = homeworkMapper.selectHomeworkById(id);
        if (homework != null) {
            // 加载题目
            List<HomeworkQuestion> questions = homeworkMapper.selectHomeworkQuestionsByHomeworkId(id);
            homework.setQuestions(questions);
        }
        return homework;
    }

    /**
     * 获取教师发布的作业列表
     */
    @Transactional(readOnly = true)
    public List<Homework> getHomeworkByTeacher(Long teacherId) {
        return homeworkMapper.selectHomeworkByTeacherId(teacherId);
    }

    /**
     * 获取课程下的所有作业
     */
    @Transactional(readOnly = true)
    public List<Homework> getHomeworkByCourse(Long courseId) {
        return homeworkMapper.selectHomeworkByCourseId(courseId);
    }

    /**
     * 获取学生可用的作业列表（未截止的）
     */
    @Transactional(readOnly = true)
    public List<Homework> getAvailableHomeworkByStudent(Long studentId) {
        return homeworkMapper.selectAvailableHomeworkByStudentId(studentId);
    }

    /**
     * 获取学生的所有作业列表
     */
    @Transactional(readOnly = true)
    public List<Homework> getHomeworkByStudent(Long studentId) {
        return homeworkMapper.selectHomeworkByStudentId(studentId);
    }

    /**
     * 发布作业（自动分配给课程内所有学生）
     */
    public void publishHomework(Long id) {
        homeworkMapper.updateHomeworkStatus(id, "PUBLISHED");
    }

    /**
     * 关闭作业
     */
    public void closeHomework(Long id) {
        homeworkMapper.updateHomeworkStatus(id, "CLOSED");
    }

    // ================ 课程相关 ================

    /**
     * 获取课程内的学生ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getStudentsByCourse(Long courseId) {
        return homeworkMapper.selectStudentIdsByCourseId(courseId);
    }

    /**
     * 获取教师的课程列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCoursesByTeacher(String teacherName) {
        return homeworkMapper.selectCoursesByTeacherId(teacherName);
    }

    /**
     * 获取学生的课程列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCoursesByStudent(Long studentId) {
        return homeworkMapper.selectCoursesByStudentId(studentId);
    }

    /**
     * 检查学生是否有权限访问课程
     */
    @Transactional(readOnly = true)
    public boolean checkStudentCourseAccess(Long studentId, Long courseId) {
        return homeworkMapper.checkStudentCourseAccess(studentId, courseId);
    }

    /**
     * 检查教师是否有权限访问课程
     */
    @Transactional(readOnly = true)
    public boolean checkTeacherCourseAccess(Long teacherId, Long courseId) {
        return homeworkMapper.checkTeacherCourseAccess(teacherId, courseId);
    }

    // ================ 作业提交 ================

    /**
     * 提交作业
     */
    public Long submitHomework(Long homeworkId, Long studentId, List<HomeworkAnswer> answers) {
        // 获取作业信息
        Homework homework = homeworkMapper.selectHomeworkById(homeworkId);
        if (homework == null) {
            throw new RuntimeException("作业不存在");
        }

        // 检查学生是否有权限访问该课程
        if (!homeworkMapper.checkStudentCourseAccess(studentId, homework.getCourseId())) {
            throw new RuntimeException("学生未选修该课程，无权限提交作业");
        }

        // 检查是否可以提交
        if (!canResubmit(homeworkId, studentId)) {
            throw new RuntimeException("超出最大提交次数或作业已截止");
        }

        // 获取当前提交次数
        int currentSubmitTimes = homeworkMapper.countSubmissionsByHomeworkIdAndStudentId(homeworkId, studentId);

        // 创建提交记录
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setHomeworkId(homeworkId);
        submission.setStudentId(studentId);
        submission.setSubmitTimes(currentSubmitTimes + 1);
        submission.setStatus("SUBMITTED");

        // 计算总分（仅针对客观题自动评分）
        BigDecimal totalScore = calculateTotalScore(homeworkId, answers);
        submission.setTotalScore(totalScore);

        homeworkMapper.insertHomeworkSubmission(submission);
        Long submissionId = submission.getId();

        // 保存答案
        if (answers != null && !answers.isEmpty()) {
            for (HomeworkAnswer answer : answers) {
                answer.setSubmissionId(submissionId);
                // 对客观题进行自动评分
                autoGradeAnswer(answer, homeworkId);
            }
            homeworkMapper.batchInsertHomeworkAnswers(answers);
        }

        return submissionId;
    }

    /**
     * 获取学生作业提交情况
     */
    @Transactional(readOnly = true)
    public List<HomeworkSubmission> getStudentSubmissions(Long homeworkId, Long studentId) {
        List<HomeworkSubmission> submissions = homeworkMapper.selectHomeworkSubmissionsByHomeworkIdAndStudentId(homeworkId, studentId);

        if (submissions != null && !submissions.isEmpty()) {
            // 为每个提交记录加载答案详情
            submissions.forEach(submission -> {
                List<HomeworkAnswer> answers = homeworkMapper.selectHomeworkAnswersBySubmissionId(submission.getId());
                submission.setAnswers(answers);
            });
        }

        return submissions;
    }

    /**
     * 获取作业的所有提交记录
     */
    @Transactional(readOnly = true)
    public List<HomeworkSubmission> getHomeworkSubmissions(Long homeworkId) {
        return homeworkMapper.selectHomeworkSubmissionsByHomeworkId(homeworkId);
    }

    /**
     * 获取学生的所有作业提交记录
     */
    @Transactional(readOnly = true)
    public List<HomeworkSubmission> getStudentSubmissions(Long studentId) {
        return homeworkMapper.selectHomeworkSubmissionsByStudentId(studentId);
    }

    /**
     * 根据ID获取提交记录
     */
    @Transactional(readOnly = true)
    public HomeworkSubmission getSubmissionById(Long submissionId) {
        return homeworkMapper.selectHomeworkSubmissionById(submissionId);
    }

    /**
     * 获取课程的所有作业提交记录
     */
    @Transactional(readOnly = true)
    public List<HomeworkSubmission> getCourseSubmissions(Long courseId) {
        return homeworkMapper.selectHomeworkSubmissionsByCourseId(courseId);
    }

    /**
     * 检查是否可以重新提交
     */
    @Transactional(readOnly = true)
    public boolean canResubmit(Long homeworkId, Long studentId) {
        // 检查作业是否还在有效期内
        Homework homework = homeworkMapper.selectHomeworkById(homeworkId);
        if (homework == null || LocalDateTime.now().isAfter(homework.getEndTime())) {
            return false;
        }

        // 检查是否允许重新提交
        if (homework.getAllowResubmit() == 0) {
            // 不允许重新提交，检查是否已经提交过
            int submitTimes = homeworkMapper.countSubmissionsByHomeworkIdAndStudentId(homeworkId, studentId);
            return submitTimes == 0;
        }

        // 允许重新提交，检查提交次数
        int submitTimes = homeworkMapper.countSubmissionsByHomeworkIdAndStudentId(homeworkId, studentId);
        return submitTimes < homework.getMaxSubmitTimes();
    }

    // ================ 作业批改 ================

    /**
     * 批改作业
     */
    public void gradeHomework(Long submissionId, List<HomeworkAnswer> gradedAnswers,
                              String feedback, Long teacherId) {
        // 更新答案得分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (HomeworkAnswer answer : gradedAnswers) {
            homeworkMapper.updateHomeworkAnswerScore(answer);
            if (answer.getScore() != null) {
                totalScore = totalScore.add(answer.getScore());
            }
        }

        // 更新提交记录
        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setId(submissionId);
        submission.setTotalScore(totalScore);
        submission.setStatus("GRADED");
        submission.setTeacherFeedback(feedback);
        submission.setGradedBy(teacherId);

        homeworkMapper.updateHomeworkGrade(submission);
    }

    /**
     * 更新单个答案得分
     */
    public void updateAnswerScore(Long answerId, Double score, Integer isCorrect, String comment) {
        HomeworkAnswer answer = new HomeworkAnswer();
        answer.setId(answerId);
        answer.setScore(BigDecimal.valueOf(score));
        answer.setIsCorrect(isCorrect);
        answer.setTeacherComment(comment);

        homeworkMapper.updateHomeworkAnswerScore(answer);
    }

    /**
     * 获取提交的答案详情
     */
    @Transactional(readOnly = true)
    public List<HomeworkAnswer> getSubmissionAnswers(Long submissionId) {
        return homeworkMapper.selectHomeworkAnswersBySubmissionId(submissionId);
    }

    /**
     * 批量返回已批改的作业给学生
     */
    public void returnGradedHomework(List<Long> submissionIds) {
        for (Long submissionId : submissionIds) {
            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setId(submissionId);
            submission.setStatus("RETURNED");
            homeworkMapper.updateHomeworkSubmission(submission);
        }
    }

    // ================ 统计分析 ================

    /**
     * 获取作业统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getHomeworkStatistics(Long homeworkId) {
        return homeworkMapper.selectHomeworkStatistics(homeworkId);
    }

    /**
     * 获取学生作业进度
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStudentProgress(Long studentId) {
        return homeworkMapper.selectStudentHomeworkProgress(studentId);
    }

    /**
     * 获取课程作业统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCourseHomeworkStatistics(Long courseId) {
        return homeworkMapper.selectCourseHomeworkStatistics(courseId);
    }

    // ================ 题目管理 ================

    /**
     * 获取作业题目列表
     */
    @Transactional(readOnly = true)
    public List<HomeworkQuestion> getHomeworkQuestions(Long homeworkId) {
        return homeworkMapper.selectHomeworkQuestionsByHomeworkId(homeworkId);
    }

    /**
     * 添加单个题目
     */
    public Long addHomeworkQuestion(HomeworkQuestion question) {
        // 获取当前最大题目顺序号
        List<HomeworkQuestion> existingQuestions = homeworkMapper.selectHomeworkQuestionsByHomeworkId(question.getHomeworkId());
        int maxOrder = existingQuestions.stream().mapToInt(HomeworkQuestion::getQuestionOrder).max().orElse(0);
        question.setQuestionOrder(maxOrder + 1);

        homeworkMapper.insertHomeworkQuestion(question);
        return question.getId();
    }

    /**
     * 更新题目
     */
    public void updateHomeworkQuestion(HomeworkQuestion question) {
        homeworkMapper.updateHomeworkQuestion(question);
    }

    /**
     * 删除题目
     */
    public void deleteHomeworkQuestion(Long questionId) {
        homeworkMapper.deleteHomeworkQuestion(questionId);
    }

    // ================ 私有辅助方法 ================

    /**
     * 计算总分（仅针对客观题）
     */
    private BigDecimal calculateTotalScore(Long homeworkId, List<HomeworkAnswer> answers) {
        BigDecimal totalScore = BigDecimal.ZERO;

        for (HomeworkAnswer answer : answers) {
            if (answer.getScore() != null) {
                totalScore = totalScore.add(answer.getScore());
            }
        }

        return totalScore;
    }

    /**
     * 自动评分（针对客观题）
     */
    private void autoGradeAnswer(HomeworkAnswer answer, Long homeworkId) {
        // 获取题目信息
        HomeworkQuestion question = homeworkMapper.selectHomeworkQuestionById(answer.getQuestionId());

        if (question != null) {
            String questionType = question.getQuestionType();

            // 只对客观题进行自动评分
            if ("SINGLE_CHOICE".equals(questionType) || "MULTIPLE_CHOICE".equals(questionType) || "FILL_BLANK".equals(questionType)) {
                String correctAnswer = question.getCorrectAnswer();
                String studentAnswer = answer.getStudentAnswer();

                if (correctAnswer != null && studentAnswer != null) {
                    boolean isCorrect = correctAnswer.trim().equalsIgnoreCase(studentAnswer.trim());
                    answer.setIsCorrect(isCorrect ? 1 : 0);
                    answer.setScore(isCorrect ? question.getScore() : BigDecimal.ZERO);
                } else {
                    answer.setIsCorrect(0);
                    answer.setScore(BigDecimal.ZERO);
                }
            } else {
                // 主观题默认设为待批改状态
                answer.setIsCorrect(null);
                answer.setScore(null);
            }
        }
    }

    /**
     * 重新计算提交记录的总分
     */
    public void recalculateSubmissionScore(Long submissionId) {
        List<HomeworkAnswer> answers = homeworkMapper.selectHomeworkAnswersBySubmissionId(submissionId);
        BigDecimal totalScore = BigDecimal.ZERO;

        for (HomeworkAnswer answer : answers) {
            if (answer.getScore() != null) {
                totalScore = totalScore.add(answer.getScore());
            }
        }

        HomeworkSubmission submission = new HomeworkSubmission();
        submission.setId(submissionId);
        submission.setTotalScore(totalScore);
        homeworkMapper.updateHomeworkSubmission(submission);
    }

    /**
     * 检查作业是否已截止
     */
    @Transactional(readOnly = true)
    public boolean isHomeworkExpired(Long homeworkId) {
        Homework homework = homeworkMapper.selectHomeworkById(homeworkId);
        return homework != null && LocalDateTime.now().isAfter(homework.getEndTime());
    }

    /**
     * 检查学生是否有权限访问作业
     */
    @Transactional(readOnly = true)
    public boolean hasHomeworkAccess(Long homeworkId, Long studentId) {
        Homework homework = homeworkMapper.selectHomeworkById(homeworkId);
        if (homework == null) {
            return false;
        }
        return homeworkMapper.checkStudentCourseAccess(studentId, homework.getCourseId());
    }

    /**
     * 检查教师是否有权限管理作业
     */
    @Transactional(readOnly = true)
    public boolean hasHomeworkManageAccess(Long homeworkId, Long teacherId) {
        Homework homework = homeworkMapper.selectHomeworkById(homeworkId);
        if (homework == null) {
            return false;
        }
        return homework.getTeacherId().equals(teacherId);
    }
}