package com.csu.sms.controller;

import com.csu.sms.dto.AILearningSuggestionDTO;
import com.csu.sms.service.AIService;
import com.csu.sms.service.GradeAnalysisService;
import com.csu.sms.service.AIBookRecommendationService;
import com.csu.sms.common.ApiResponse;
import com.csu.sms.util.UserContext;
import com.csu.sms.annotation.LogOperation;
import com.csu.sms.vo.AIBookRecommendationVO;
import com.csu.sms.dto.StudyRecordDTO;
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

    @Autowired
    private AIBookRecommendationService bookRecommendationService;

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
            
            // 获取学习记录和考试记录（补充详细数据）
            List<StudyRecordDTO> studyRecordDTOs = gradeAnalysisService.getUserStudyRecords(userId, 1, 10, startDate, endDate).getList();
            List<AILearningSuggestionDTO.StudyRecordInfo> studyRecords = new java.util.ArrayList<>();
            for (StudyRecordDTO dto : studyRecordDTOs) {
                AILearningSuggestionDTO.StudyRecordInfo info = new AILearningSuggestionDTO.StudyRecordInfo();
                info.setId(dto.getVideoId());
                info.setCourseTitle(dto.getCourseTitle());
                info.setVideoTitle(dto.getVideoTitle());
                info.setProgress(dto.getProgress() != null ? dto.getProgress().longValue() : 0L);
                // 进度百分比可选
                info.setProgressPercentage(dto.getVideoDuration() != null && dto.getVideoDuration() > 0 ? (dto.getProgress() * 100.0 / dto.getVideoDuration()) : 0.0);
                info.setLastStudyTime(dto.getLastStudyTime());
                studyRecords.add(info);
            }
            request.setStudyRecords(studyRecords);
            
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

    /**
     * 生成AI书籍推荐
     * @return 生成的书籍推荐列表
     */
    @PostMapping("/book-recommendations/generate")
    @LogOperation(module = "AI服务", operation = "生成书籍推荐")
    public ApiResponse<List<AIBookRecommendationVO>> generateBookRecommendations() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }
        List<AIBookRecommendationVO> recommendations = bookRecommendationService.generateRecommendationsForStudent(userId);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取当前用户的所有书籍推荐
     * @return 书籍推荐列表
     */
    @GetMapping("/book-recommendations")
    public ApiResponse<List<AIBookRecommendationVO>> getBookRecommendations() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }
        List<AIBookRecommendationVO> recommendations = bookRecommendationService.getRecommendationsForStudent(userId);
        return ApiResponse.success(recommendations);
    }

    /**
     * 获取特定课程的书籍推荐
     * @param courseId 课程ID
     * @return 书籍推荐列表
     */
    @GetMapping("/book-recommendations/course/{courseId}")
    public ApiResponse<List<AIBookRecommendationVO>> getBookRecommendationsByCourse(@PathVariable Long courseId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }
        List<AIBookRecommendationVO> recommendations = bookRecommendationService.getRecommendationsByCourse(courseId, userId);
        return ApiResponse.success(recommendations);
    }

    /**
     * 标记推荐为已读
     * @param id 推荐ID
     * @return 操作结果
     */
    @PostMapping("/book-recommendations/{id}/read")
    public ApiResponse<Boolean> markRecommendationAsRead(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }
        boolean result = bookRecommendationService.markAsRead(id, userId);
        return ApiResponse.success(result);
    }

    /**
     * 删除推荐记录
     * @param id 推荐记录ID
     * @return 操作结果
     */
    @DeleteMapping("/book-recommendations/{id}")
    @LogOperation(module = "AI服务", operation = "删除书籍推荐")
    public ApiResponse<Boolean> deleteRecommendation(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }
        
        try {
            boolean result = bookRecommendationService.deleteRecommendation(id, userId);
            if (result) {
                return ApiResponse.success("删除推荐记录成功", true);
            } else {
                return ApiResponse.error("删除失败：推荐记录不存在或无权限删除");
            }
        } catch (Exception e) {
            return ApiResponse.error("删除推荐记录失败：" + e.getMessage());
        }
    }
}