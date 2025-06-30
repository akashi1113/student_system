package com.csu.sms.controller;

import com.csu.sms.dto.*;
import com.csu.sms.dto.exam.QuestionAnalysisDTO;
import com.csu.sms.dto.exam.QuestionCreateDTO;
import com.csu.sms.dto.exam.QuestionResponseDTO;
import com.csu.sms.model.question.Question;
import com.csu.sms.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class QuestionController {

    @Autowired
    private QuestionService questionService;

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
                                                                       Authentication auth) {
        try {
            // 验证权限：只有教师和管理员可以查看答案
            if (!hasManagePermission(auth)) {
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
    public ApiResponse<Question> getQuestionById(@PathVariable Long id, Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
                                                Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
                                                       Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
                                                Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id, Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
    public ApiResponse<Void> deleteQuestions(@RequestBody List<Long> ids, Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
    public ApiResponse<Void> deleteQuestionsByExamId(@PathVariable Long examId, Authentication auth) {
        try {
            // 检查权限
            if (!hasManagePermission(auth)) {
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
                                                                      Authentication auth) {
        try {
            // 学生只能查看自己的分析，教师可以查看所有
            Long userId = getUserIdFromAuth(auth);
            if (!hasManagePermission(auth) && !isOwnerOfExamRecord(examRecordId, userId)) {
                return ApiResponse.error("权限不足", "PERMISSION_DENIED");
            }

            List<QuestionAnalysisDTO> analysis = questionService.getQuestionAnalysis(examRecordId);
            return ApiResponse.success("获取答题分析成功", analysis);
        } catch (Exception e) {
            return ApiResponse.error("获取答题分析失败: " + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    // 检查是否有管理权限
    private boolean hasManagePermission(Authentication auth) {
        if (auth == null) return false;

        // 简化处理：从JWT token中获取角色信息
        // 实际项目中应该从SecurityContext中获取用户角色
        String role = getRoleFromAuth(auth);
        return "TEACHER".equals(role) || "ADMIN".equals(role);
    }

    // 从认证信息中获取用户ID
    private Long getUserIdFromAuth(Authentication auth) {
        // 实际项目中应该从JWT token中解析用户ID
        return 1L; // 简化处理
    }

    // 从认证信息中获取用户角色
    private String getRoleFromAuth(Authentication auth) {
        // 实际项目中应该从JWT token中解析用户角色
        return "STUDENT"; // 简化处理
    }

    // 检查用户是否为考试记录的所有者
    private boolean isOwnerOfExamRecord(Long examRecordId, Long userId) {
        // 实际项目中应该查询数据库验证
        return true; // 简化处理
    }
}