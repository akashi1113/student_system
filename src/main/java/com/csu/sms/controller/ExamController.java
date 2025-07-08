package com.csu.sms.controller;

import com.csu.sms.dto.exam.AnswerDTO;
import com.csu.sms.dto.ApiResponse;
import com.csu.sms.dto.exam.CreateExamRequest;
import com.csu.sms.dto.exam.QuestionCreateDTO;
import com.csu.sms.dto.exam.ViolationRequest;
import com.csu.sms.dto.ExamListDTO;
import com.csu.sms.model.booking.ExamBooking;
import com.csu.sms.model.exam.Exam;
import com.csu.sms.model.exam.ExamRecord;
import com.csu.sms.model.exam.ExamScoreResult;
import com.csu.sms.model.question.Question;
import com.csu.sms.service.ExamBookingService;
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
    private ExamBookingService examBookingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ApiResponse<List<Exam>> getAllExams() {
        return ApiResponse.success(examService.getAllAvailableExams());
    }

    @GetMapping("/list")
    public ApiResponse<List<ExamListDTO>> getExamList() {
        try {
            List<ExamListDTO> examList = examService.getExamList();
            return ApiResponse.success(examList);
        } catch (Exception e) {
            return ApiResponse.error("获取考试列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/bookable")
    public ApiResponse<List<Exam>> getBookableExams() {
        return ApiResponse.success(examService.getBookableExams());
    }

    @GetMapping("/booked")
    public ApiResponse<List<Exam>> getBookedExams(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        List<Exam> exams=examService.getBookedExams(userId);
        exams.forEach(exam -> {
            ExamBooking booking = examBookingService.getBookingByUserAndExam(userId,exam.getId());
            booking.setTimeSlot(examBookingService.getTimeSlotById(booking.getTimeSlotId()));
            exam.setExamBooking(booking);
        });
        return ApiResponse.success(exams);
    }

    @GetMapping("/{examId}")
    public ApiResponse<Exam> getExamById(@PathVariable("examId") Long examId) {
        Exam exam = examService.getExamById(examId);
        return ApiResponse.success(exam);
    }

    @PostMapping("/{examId}/start")
    @LogOperation(module = "考试管理", operation = "开始考试", description = "学生开始考试")
    public ApiResponse<ExamRecord> startExam(@PathVariable Long examId,
                                             @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        if(!examService.canStartExam(examId, userId)) {
            return ApiResponse.success("您已经完成该考试，无法再次开始",examService.getExamRecord(examId, userId));
        }
        ExamRecord record = examService.startExam(examId, userId);
        return ApiResponse.success(record);
    }

    @GetMapping("/{examId}/status")
    public ApiResponse<ExamRecord> getExamStatus(@PathVariable Long examId,
                                                 @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.extractUserId(token);
        ExamRecord record = examService.getExamRecord(examId, userId);
        return ApiResponse.success(record);
    }

    @PostMapping("/{examId}/submit")
    @LogOperation(module = "考试管理", operation = "提交考试", description = "学生提交考试答案")
    public ApiResponse<ExamScoreResult> submitExam(@PathVariable Long examId,
                                                   @RequestBody List<AnswerDTO> answers,
                                                   @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token);

            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.error("考试已结束，无法提交答案", "EXAM_ALREADY_ENDED");
            }

            questionService.scoreAnswersWithAI(examRecord.getId(), answers);
            int totalScore = examService.calculateTotalScore(examRecord.getId());

            if (examRecord.getStartTime() != null) {
                long duration = java.time.Duration.between(
                        examRecord.getStartTime(),
                        LocalDateTime.now()
                ).toSeconds();
                examRecord.setDuration((int) duration);
            }

            examRecord.setScore(totalScore);
            examRecord.setStatus("SUBMITTED");
            examRecord.setSubmitTime(LocalDateTime.now());
            examService.updateExamRecord(examRecord);

            ExamScoreResult result = new ExamScoreResult();
            result.setExamRecordId(examRecord.getId());
            result.setTotalScore(totalScore);
            result.setMaxScore(questionService.getTotalScore(examId));
            result.setPassingScore((int) (result.getMaxScore() * 0.6));
            result.setPassed(totalScore >= result.getPassingScore());

            return ApiResponse.success("提交考试成功", result);

        } catch (Exception e) {
            return ApiResponse.error("提交考试失败: " + e.getMessage());
        }
    }

    @PostMapping("/{examId}/violation")
    @LogOperation(module = "考试管理", operation = "记录违规", description = "记录考试违规行为")
    public ApiResponse<Void> recordViolation(@PathVariable Long examId,
                                             @RequestBody ViolationRequest violation,
                                             @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token);

            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if (!"IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.success("考试已结束", null);
            }

            int currentViolations = examRecord.getViolationCount() != null ? examRecord.getViolationCount() : 0;
            examRecord.setViolationCount(currentViolations + 1);

            if (examRecord.getViolationCount() >= 3) {
                examService.autoSubmitExam(examId, userId, "VIOLATION");
                return ApiResponse.success("违规次数过多，考试已自动提交", null);
            } else {
                examService.updateExamRecord(examRecord);
                return ApiResponse.success("违规记录已保存", null);
            }

        } catch (Exception e) {
            return ApiResponse.error("记录违规失败: " + e.getMessage());
        }
    }

    @PostMapping("/{examId}/timeout")
    @LogOperation(module = "考试管理", operation = "超时处理", description = "考试超时自动提交")
    public ApiResponse<ExamScoreResult> handleTimeout(@PathVariable Long examId,
                                                      @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token);

            ExamRecord examRecord = examService.autoSubmitExam(examId, userId, "TIMEOUT");

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

    @GetMapping("/{examId}/score")
    public ApiResponse<ExamScoreResult> getExamScore(@PathVariable Long examId,
                                                     @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token);
            ExamRecord examRecord = examService.getExamRecord(examId, userId);
            if (examRecord == null) {
                return ApiResponse.error("考试记录不存在", "EXAM_RECORD_NOT_FOUND");
            }

            if ("IN_PROGRESS".equals(examRecord.getStatus())) {
                return ApiResponse.error("考试尚未结束", "EXAM_IN_PROGRESS");
            }

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

    @PostMapping
    @LogOperation(module = "考试管理", operation = "创建考试", description = "教师创建新考试")
    public ApiResponse<Exam> createExam(@RequestBody CreateExamRequest request,
                                        @RequestHeader("Authorization") String token) {
        try {
            Long teacherId = jwtUtil.extractUserId(token);

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

            if ("ONLINE".equals(request.getExamMode())) {
                if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
                    return ApiResponse.error("线上考试必须包含题目", "NO_QUESTIONS_FOR_ONLINE");
                }
            }

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
            exam.setStatus("DRAFT");
            exam.setBookingStatus("UNAVAILABLE");

            if ("ONLINE".equals(request.getExamMode()) && request.getQuestions() != null) {
                int totalScore = request.getQuestions().stream()
                        .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
                        .sum();
                exam.setTotalScore(totalScore);
            } else {
                exam.setTotalScore(request.getPassingScore() != null ?
                        (int)(request.getPassingScore() / 0.6) : 100);
            }

            Exam createdExam = examService.createExam(exam);

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

    @GetMapping("/created")
    public ApiResponse<List<Exam>> getCreatedExams(@RequestHeader("Authorization") String token,
                                                   @RequestParam(defaultValue = "ALL") String status) {
        try {
            Long teacherId = jwtUtil.extractUserId(token);
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

    @PostMapping("/{examId}/questions")
    @LogOperation(module = "考试管理", operation = "添加题目", description = "为线上考试添加题目")
    public ApiResponse<Question> addQuestionToExam(@PathVariable Long examId,
                                                   @RequestBody QuestionCreateDTO questionDTO,
                                                   @RequestHeader("Authorization") String token) {
        try {
            Long teacherId = jwtUtil.extractUserId(token);

            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                return ApiResponse.error("考试不存在", "EXAM_NOT_FOUND");
            }

            if (!exam.getCreatedBy().equals(teacherId)) {
                return ApiResponse.error("无权限操作此考试", "PERMISSION_DENIED");
            }

            if (!"ONLINE".equals(exam.getExamMode())) {
                return ApiResponse.error("只有线上考试可以添加题目", "OFFLINE_EXAM_NO_QUESTIONS");
            }

            if ("PUBLISHED".equals(exam.getStatus())) {
                return ApiResponse.error("已发布的考试不能修改题目", "PUBLISHED_EXAM_READONLY");
            }

            questionDTO.setExamId(examId);
            Question question = questionService.createQuestion(questionDTO);

            Integer newTotalScore = questionService.getTotalScore(examId);
            exam.setTotalScore(newTotalScore);
            examService.updateExam(exam);

            return ApiResponse.success("题目添加成功", question);

        } catch (Exception e) {
            return ApiResponse.error("添加题目失败: " + e.getMessage());
        }
    }

    @PostMapping("/{examId}/publish")
    @LogOperation(module = "考试管理", operation = "发布考试", description = "教师发布考试")
    public ApiResponse<Exam> publishExam(@PathVariable Long examId,
                                         @RequestHeader("Authorization") String token) {
        try {
            Long teacherId = jwtUtil.extractUserId(token);

            Exam exam = examService.getExamById(examId);
            if (exam == null) {
                return ApiResponse.error("考试不存在", "EXAM_NOT_FOUND");
            }

            if (!exam.getCreatedBy().equals(teacherId)) {
                return ApiResponse.error("无权限发布此考试", "PERMISSION_DENIED");
            }

            if (!"DRAFT".equals(exam.getStatus())) {
                return ApiResponse.error("只有草稿状态的考试可以发布", "INVALID_STATUS");
            }

            if ("ONLINE".equals(exam.getExamMode())) {
                Integer totalScore = questionService.getTotalScore(examId);
                if (totalScore == null || totalScore == 0) {
                    return ApiResponse.error("线上考试必须包含题目才能发布", "NO_QUESTIONS");
                }
            }

            exam.setStatus("PUBLISHED");
            Exam publishedExam = examService.updateExam(exam);

            return ApiResponse.success("考试发布成功，请设置考试时间段后开放预约", publishedExam);

        } catch (Exception e) {
            return ApiResponse.error("发布考试失败: " + e.getMessage());
        }
    }
}