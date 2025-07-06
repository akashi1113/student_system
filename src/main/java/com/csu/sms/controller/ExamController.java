package com.csu.sms.controller;

import com.csu.sms.dto.exam.AnswerDTO;
import com.csu.sms.dto.ApiResponse;
import com.csu.sms.dto.exam.CreateExamRequest;
import com.csu.sms.dto.exam.QuestionCreateDTO;
import com.csu.sms.dto.exam.ViolationRequest;
import com.csu.sms.model.exam.Exam;
import com.csu.sms.model.exam.ExamRecord;
import com.csu.sms.model.exam.ExamScoreResult;
import com.csu.sms.model.question.Question;
import com.csu.sms.service.ExamService;
import com.csu.sms.service.QuestionService;
import com.csu.sms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.csu.sms.annotation.LogOperation;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/exams")
@CrossOrigin(origins = "http://localhost:5173")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<Exam>> getAllExams() {
        return ApiResponse.success(examService.getAllAvailableExams());
    }

    @GetMapping("/bookable")
    public ApiResponse<List<Exam>> getBookableExams() {
        return ApiResponse.success(examService.getBookableExams());
    }

    @GetMapping("/booked")
    public ApiResponse<List<Exam>> getBookedExams(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        return ApiResponse.success(examService.getBookedExams(userId));
    }

    @GetMapping("/{examId}")
    public ApiResponse<Exam> getExamById(@PathVariable("examId") Long examId) {
        Exam exam = examService.getExamById(examId);
        return ApiResponse.success(exam);
    }

    @PostMapping("/{examId}/start")
    @LogOperation(module = "考试管理", operation = "开始考试", description = "学生开始考试")
    public ApiResponse<ExamRecord> startExam(@PathVariable Long examId,
                                             Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if(!examService.canStartExam(examId, userId)) {
            return ApiResponse.success("您已经完成该考试，无法再次开始",examService.getExamRecord(examId, userId));
        }
        ExamRecord record = examService.startExam(examId, userId);
        return ApiResponse.success(record);
    }

    @GetMapping("/{examId}/status")
    public ApiResponse<ExamRecord> getExamStatus(@PathVariable Long examId,
                                                 Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        ExamRecord record = examService.getExamRecord(examId, userId);
        return ApiResponse.success(record);
    }

    private Long getUserIdFromAuth(Authentication auth) {
        // 从JWT token中获取用户ID
        return 3L; // 简化处理
    }

    // 提交考试答案
    @PostMapping("/{examId}/submit")
    @LogOperation(module = "考试管理", operation = "提交考试", description = "学生提交考试答案")
    public ApiResponse<ExamScoreResult> submitExam(@PathVariable Long examId,
                                                   @RequestBody List<AnswerDTO> answers,
                                                   Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            // 获取考试记录
            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.error("考试已结束，无法提交答案", "EXAM_ALREADY_ENDED");
            }

            // 保存答案并评分
            questionService.scoreAnswersWithAI(examRecord.getId(), answers);

            // 计算总分
            int totalScore = examService.calculateTotalScore(examRecord.getId());

            // 计算实际用时
            if (examRecord.getStartTime() != null) {
                long duration = java.time.Duration.between(
                        examRecord.getStartTime(),
                        LocalDateTime.now()
                ).toSeconds();
                examRecord.setDuration((int) duration);
            }

            // 更新考试记录
            examRecord.setScore(totalScore);
            examRecord.setStatus("SUBMITTED");
            examRecord.setSubmitTime(LocalDateTime.now());
            examService.updateExamRecord(examRecord);

            // 构建返回结果
            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(totalScore);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6)); // 60%及格
            result.setPassed(totalScore >= result.getPassingScore());

            return ApiResponse.success("提交考试成功", result);

        } catch (Exception e) {
            return ApiResponse.error("提交考试失败: " + e.getMessage());
        }
    }

    // 记录违规行为
    @PostMapping("/{examId}/violation")
    @LogOperation(module = "考试管理", operation = "记录违规", description = "记录考试违规行为")
    public ApiResponse<Void> recordViolation(@PathVariable Long examId,
                                             @RequestBody ViolationRequest violation,
                                             Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            // 获取考试记录
            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.success("考试已结束", null);
            }

            // 增加违规次数
            int currentViolations = examRecord.getViolationCount() != null ? examRecord.getViolationCount() : 0;
            examRecord.setViolationCount(currentViolations + 1);

            // 检查是否达到违规上限
            if (examRecord.getViolationCount() >= 3) {
                // 自动提交考试
                examService.autoSubmitExam(examId, userId, "VIOLATION");
                return ApiResponse.success("违规次数过多，考试已自动提交", null);
            } else {
                // 只更新违规次数
                examService.updateExamRecord(examRecord);
                return ApiResponse.success("违规记录已保存", null);
            }

        } catch (Exception e) {
            return ApiResponse.error("记录违规失败: " + e.getMessage());
        }
    }

    // 考试超时处理
    @PostMapping("/{examId}/timeout")
    @LogOperation(module = "考试管理", operation = "超时处理", description = "考试超时自动提交")
    public ApiResponse<ExamScoreResult> handleTimeout(@PathVariable Long examId,
                                                      Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            // 自动提交考试
            ExamRecord examRecord = examService.autoSubmitExam(examId, userId, "TIMEOUT");

            // 构建返回结果
            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(examRecord.getScore() != null ? examRecord.getScore() : 0);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6));
            result.setPassed(result.getTotalScore() >= result.getPassingScore());

            return ApiResponse.success("考试时间到，已自动提交", result);

        } catch (Exception e) {
            return ApiResponse.error("处理考试超时失败: " + e.getMessage());
        }
    }

    // 获取考试成绩
    @GetMapping("/{examId}/score")
    public ApiResponse<ExamScoreResult> getExamScore(@PathVariable Long examId,
                                                     Authentication auth) {
        try {
            Long userId = getUserIdFromAuth(auth);

            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if ("IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.error("考试尚未结束", "EXAM_IN_PROGRESS");
            }

            // 构建成绩结果
            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(examRecord.getScore() != null ? examRecord.getScore() : 0);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6));
            result.setPassed(result.getTotalScore() >= result.getPassingScore());

            return ApiResponse.success("获取成绩成功", result);

        } catch (Exception e) {
            return ApiResponse.error("获取成绩失败: " + e.getMessage());
        }
    }

    // 创建考试
    @PostMapping
    @LogOperation(module = "考试管理", operation = "创建考试", description = "教师创建新考试")
    public ApiResponse<Exam> createExam(@RequestBody CreateExamRequest request,
                                        Authentication auth) {
        try {
            Long teacherId = getUserIdFromAuth(auth);

            // 验证请求参数
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ApiResponse.error("考试标题不能为空", "INVALID_TITLE");
            }

            if (request.getDuration() == null || request.getDuration() <= 0) {
                return ApiResponse.error("考试时长必须大于0", "INVALID_DURATION");
            }

            if (request.getExamMode() == null ||
                    (!request.getExamMode().equals("ONLINE") && !request.getExamMode().equals("OFFLINE"))) {
                return ApiResponse.error("考试模式必须为 ONLINE 或 OFFLINE", "INVALID_EXAM_MODE");
            }

            // 线上考试必须包含题目
            if ("ONLINE".equals(request.getExamMode())) {
                if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
                    return ApiResponse.error("线上考试必须包含题目", "NO_QUESTIONS_FOR_ONLINE");
                }
            }

            // 创建考试对象
            Exam exam = new Exam();
            exam.setTitle(request.getTitle());
            exam.setDescription(request.getDescription());
            exam.setDuration(request.getDuration());
            exam.setExamMode(request.getExamMode());
            exam.setType(request.getType() != null ? request.getType() : "REGULAR");
            exam.setPassingScore(request.getPassingScore() != null ? request.getPassingScore() : 60);
            exam.setMaxAttempts(request.getMaxAttempts() != null ? request.getMaxAttempts() : 1);
            exam.setCourseId(request.getCourseId());
            exam.setCreatedBy(teacherId);
            exam.setStatus("DRAFT"); // 初始状态为草稿
            exam.setBookingStatus("UNAVAILABLE"); // 初始不可预约，需要设置时间段后才可预约

            // 计算总分（如果是线上考试）
            if ("ONLINE".equals(request.getExamMode()) && request.getQuestions() != null) {
                int totalScore = request.getQuestions().stream()
                        .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
                        .sum();
                exam.setTotalScore(totalScore);
            } else {
                // 线下考试总分后续设置
                exam.setTotalScore(request.getPassingScore() != null ?
                        (int)(request.getPassingScore() / 0.6) : 100);
            }

            // 保存考试
            Exam createdExam = examService.createExam(exam);

            // 如果是线上考试，创建题目
            if ("ONLINE".equals(request.getExamMode()) && request.getQuestions() != null) {
                for (int i = 0; i < request.getQuestions().size(); i++) {
                    QuestionCreateDTO questionDTO = request.getQuestions().get(i);
                    questionDTO.setExamId(createdExam.getId());
                    questionDTO.setOrderNum(i + 1);
                    questionService.createQuestion(questionDTO);
                }
            }

            return ApiResponse.success("考试创建成功", createdExam);

        } catch (Exception e) {
            return ApiResponse.error("创建考试失败: " + e.getMessage());
        }
    }

    // 获取教师创建的考试列表
    @GetMapping("/created")
    public ApiResponse<List<Exam>> getCreatedExams(Authentication auth,
                                                   @RequestParam(defaultValue = "ALL") String status) {
        try {
            Long teacherId = getUserIdFromAuth(auth);
            List<Exam> exams;

            if ("ALL".equals(status)) {
                exams = examService.getExamsByCreator(teacherId);
            } else {
                exams = examService.getExamsByCreatorAndStatus(teacherId, status);
            }

            return ApiResponse.success(exams);
        } catch (Exception e) {
            return ApiResponse.error("获取考试列表失败: " + e.getMessage());
        }
    }

    // 添加/编辑考试题目（仅限线上考试）
    @PostMapping("/{examId}/questions")
    @LogOperation(module = "考试管理", operation = "添加题目", description = "为线上考试添加题目")
    public ApiResponse<Question> addQuestionToExam(@PathVariable Long examId,
                                                   @RequestBody QuestionCreateDTO questionDTO,
                                                   Authentication auth) {
        try {
            Long teacherId = getUserIdFromAuth(auth);

            // 获取考试信息
            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                return ApiResponse.error("考试不存在", "EXAM_NOT_FOUND");
            }

            // 验证权限
            if (!exam.getCreatedBy().equals(teacherId)) {
                return ApiResponse.error("无权限操作此考试", "PERMISSION_DENIED");
            }

            // 验证考试模式
            if (!"ONLINE".equals(exam.getExamMode())) {
                return ApiResponse.error("只有线上考试可以添加题目", "OFFLINE_EXAM_NO_QUESTIONS");
            }

            // 验证考试状态
            if ("PUBLISHED".equals(exam.getStatus())) {
                return ApiResponse.error("已发布的考试不能修改题目", "PUBLISHED_EXAM_READONLY");
            }

            questionDTO.setExamId(examId);
            Question question = questionService.createQuestion(questionDTO);

            // 重新计算考试总分
            Integer newTotalScore = questionService.getTotalScore(examId);
            exam.setTotalScore(newTotalScore);
            examService.updateExam(exam);

            return ApiResponse.success("题目添加成功", question);

        } catch (Exception e) {
            return ApiResponse.error("添加题目失败: " + e.getMessage());
        }
    }

    // 发布考试
    @PostMapping("/{examId}/publish")
    @LogOperation(module = "考试管理", operation = "发布考试", description = "教师发布考试")
    public ApiResponse<Exam> publishExam(@PathVariable Long examId,
                                         Authentication auth) {
        try {
            Long teacherId = getUserIdFromAuth(auth);

            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                return ApiResponse.error("考试不存在", "EXAM_NOT_FOUND");
            }

            // 验证权限
            if (!exam.getCreatedBy().equals(teacherId)) {
                return ApiResponse.error("无权限发布此考试", "PERMISSION_DENIED");
            }

            // 验证考试状态
            if (!"DRAFT".equals(exam.getStatus())) {
                return ApiResponse.error("只有草稿状态的考试可以发布", "INVALID_STATUS");
            }

            // 验证线上考试是否有题目
            if ("ONLINE".equals(exam.getExamMode())) {
                Integer totalScore = questionService.getTotalScore(examId);
                if (totalScore == null || totalScore == 0) {
                    return ApiResponse.error("线上考试必须包含题目才能发布", "NO_QUESTIONS");
                }
            }

            // 发布考试
            exam.setStatus("PUBLISHED");
            // 注意：这里不设置为 AVAILABLE，因为还需要设置考试时间段
            Exam publishedExam = examService.updateExam(exam);

            return ApiResponse.success("考试发布成功，请设置考试时间段后开放预约", publishedExam);

        } catch (Exception e) {
            return ApiResponse.error("发布考试失败: " + e.getMessage());
        }
    }
}