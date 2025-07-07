package com.csu.sms.controller;

import com.csu.sms.dto.*;
import com.csu.sms.dto.exam.QuestionAnalysisDTO;
import com.csu.sms.dto.exam.QuestionCreateDTO;
import com.csu.sms.dto.exam.QuestionResponseDTO;
import com.csu.sms.model.question.Question;
import com.csu.sms.service.ExamService;
import com.csu.sms.service.QuestionService;
import com.csu.sms.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamService examService;

    @Autowired
    private JwtUtil jwtUtil;

    // 根据考试ID获取题目列表（考试用）
    @GetMapping("/exam/{examId}")
    public ApiResponse<List<QuestionResponseDTO>> getQuestionsByExamId(@PathVariable Long examId) {
        try {
            List<QuestionResponseDTO> questions = questionService.getQuestionsByExamId(examId);
            return ApiResponse.success("获取题目成功", questions);
        } catch (Exception e) {
            return ApiResponse.error("获取题目失败: " + e.getMessage());
        }
    }

    // 根据考试ID获取题目列表（管理用，含答案）
    @GetMapping("/exam/{examId}/manage")
    public ApiResponse<List<Question>> getQuestionsWithAnswersByExamId(@PathVariable Long examId,
                                                                       @RequestHeader("Authorization") String token) {
        try {
            // 验证权限：只有教师和管理员可以查看答案
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            List<Question> questions = questionService.getQuestionsWithAnswersByExamId(examId);
            return ApiResponse.success("获取题目成功", questions);
        } catch (Exception e) {
            return ApiResponse.error("获取题目失败: " + e.getMessage());
        }
    }

    // 根据题目ID获取题目详情
    @GetMapping("/{id}")
    public ApiResponse<Question> getQuestionById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            Question question = questionService.getQuestionById(id);
            if (question == null) {
                return ApiResponse.error("题目不存在", "QUESTION_NOT_FOUND");
            }

            return ApiResponse.success("获取题目详情成功", question);
        } catch (Exception e) {
            return ApiResponse.error("获取题目详情失败: " + e.getMessage());
        }
    }

    // 创建题目
    @PostMapping
    public ApiResponse<Question> createQuestion(@Valid @RequestBody QuestionCreateDTO questionDTO,
                                                @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            Question question = questionService.createQuestion(questionDTO);
            return ApiResponse.success("创建题目成功", question);
        } catch (RuntimeException e) {
            return ApiResponse.error("创建题目失败: " + e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            return ApiResponse.error("创建题目失败: " + e.getMessage());
        }
    }

    // 批量创建题目
    @PostMapping("/batch")
    public ApiResponse<List<Question>> createQuestions(@Valid @RequestBody List<QuestionCreateDTO> questionDTOs,
                                                       @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            List<Question> questions = questionDTOs.stream()
                    .map(dto -> questionService.createQuestion(dto))
                    .toList();

            return ApiResponse.success("批量创建题目成功", questions);
        } catch (RuntimeException e) {
            return ApiResponse.error("批量创建题目失败: " + e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            return ApiResponse.error("批量创建题目失败: " + e.getMessage());
        }
    }

    // 更新题目
    @PutMapping("/{id}")
    public ApiResponse<Question> updateQuestion(@PathVariable Long id,
                                                @Valid @RequestBody QuestionCreateDTO questionDTO,
                                                @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            Question question = questionService.updateQuestion(id, questionDTO);
            return ApiResponse.success("更新题目成功", question);
        } catch (RuntimeException e) {
            return ApiResponse.error("更新题目失败: " + e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            return ApiResponse.error("更新题目失败: " + e.getMessage());
        }
    }

    // 删除题目
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            questionService.deleteQuestion(id);
            return ApiResponse.success("删除题目成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除题目失败: " + e.getMessage());
        }
    }

    // 批量删除题目
    @DeleteMapping("/batch")
    public ApiResponse<Void> deleteQuestions(@RequestBody List<Long> ids, @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            for (Long id : ids) {
                questionService.deleteQuestion(id);
            }

            return ApiResponse.success("批量删除题目成功", null);
        } catch (Exception e) {
            return ApiResponse.error("批量删除题目失败: " + e.getMessage());
        }
    }

    // 根据考试ID删除所有题目
    @DeleteMapping("/exam/{examId}")
    public ApiResponse<Void> deleteQuestionsByExamId(@PathVariable Long examId, @RequestHeader("Authorization") String token) {
        try {
            // 检查权限
            if (!hasManagePermission(token)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            questionService.deleteQuestionsByExamId(examId);
            return ApiResponse.success("删除考试所有题目成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除考试题目失败: " + e.getMessage());
        }
    }

    // 获取考试总分
    @GetMapping("/exam/{examId}/total-score")
    public ApiResponse<Integer> getTotalScore(@PathVariable Long examId) {
        try {
            Integer totalScore = questionService.getTotalScore(examId);
            return ApiResponse.success("获取总分成功", totalScore);
        } catch (Exception e) {
            return ApiResponse.error("获取总分失败: " + e.getMessage());
        }
    }

    // 获取答题分析结果
    @GetMapping("/analysis/{examRecordId}")
    public ApiResponse<List<QuestionAnalysisDTO>> getQuestionAnalysis(@PathVariable Long examRecordId,
                                                                      @RequestHeader("Authorization") String token) {
        try {
            // 学生只能查看自己的分析，教师可以查看所有
            Long userId = jwtUtil.extractUserId(token);

            if (!hasManagePermission(token) && !examService.getExamRecordById(examRecordId).getUserId().equals(userId)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            List<QuestionAnalysisDTO> analysis = questionService.getQuestionAnalysis(examRecordId);
            return ApiResponse.success("获取答题分析成功", analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取答题分析失败: " + e.getMessage());
        }
    }

    // 检查是否有管理权限
    private boolean hasManagePermission(String token) {
        if (token == null) return false;
        String role = jwtUtil.extractRole(token);
        return "teacher".equals(role) || "admin".equals(role);
    }
}