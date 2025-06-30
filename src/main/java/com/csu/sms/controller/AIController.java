package com.csu.sms.controller;

import com.csu.sms.dto.AILearningSuggestionDTO;
import com.csu.sms.service.AIService;
import com.csu.sms.service.GradeAnalysisService;
import com.csu.sms.common.ApiResponse;
import com.csu.sms.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI学习建议控制器
 * @author CSU Team
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AIController {

    @Autowired
    private AIService aiService;
    
    @Autowired
    private GradeAnalysisService gradeAnalysisService;

    /**
     * 获取当前用户的个性化学习建议
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @return 学习建议列表
     */
    @GetMapping("/learning-suggestions")
    public ApiResponse<List<AILearningSuggestionDTO.Suggestion>> getCurrentUserLearningSuggestions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        try {
            Long userId = UserContext.getRequiredCurrentUserId();
            
            // 获取用户的学习分析数据
            var analysis = gradeAnalysisService.getUserAnalysis(userId, startDate, endDate);
            
            // 构建AI请求对象
            AILearningSuggestionDTO.Request request = new AILearningSuggestionDTO.Request();
            request.setUserId(userId);
            request.setAverageScore(analysis.getAverageScore() != null ? analysis.getAverageScore().doubleValue() : 0.0);
            request.setTotalStudyTime(analysis.getTotalStudyDuration() != null ? analysis.getTotalStudyDuration() : 0L);
            request.setExamPassRate(analysis.getPassRate() != null ? analysis.getPassRate().doubleValue() : 0.0);
            
            // 获取学习记录和考试记录（这里简化处理，实际可以根据需要获取详细数据）
            // 可以调用gradeAnalysisService的相关方法获取详细的学习和考试记录
            
            List<AILearningSuggestionDTO.Suggestion> suggestions = aiService.getLearningSuggestions(request);
            return ApiResponse.success("获取学习建议成功", suggestions);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("获取学习建议失败：" + e.getMessage());
        }
    }

    /**
     * 获取个性化学习建议（原有接口，保留兼容性）
     * @param request 学习数据请求
     * @return 学习建议列表
     */
    @PostMapping("/learning-suggestions")
    public ApiResponse<List<AILearningSuggestionDTO.Suggestion>> getLearningSuggestions(
            @Valid @RequestBody AILearningSuggestionDTO.Request request) {
        try {
            List<AILearningSuggestionDTO.Suggestion> suggestions = aiService.getLearningSuggestions(request);
            return ApiResponse.success("获取学习建议成功", suggestions);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("获取学习建议失败：" + e.getMessage());
        }
    }
}