package com.csu.sms.service;

import com.csu.sms.vo.AIBookRecommendationVO;
import java.util.List;

public interface AIBookRecommendationService {
    
    /**
     * 为学生生成课程相关的书籍推荐
     * @param userId 学生ID
     * @return 推荐书籍列表
     */
    List<AIBookRecommendationVO> generateRecommendationsForStudent(Long userId);
    
    /**
     * 获取学生的所有书籍推荐
     * @param userId 学生ID
     * @return 推荐书籍列表
     */
    List<AIBookRecommendationVO> getRecommendationsForStudent(Long userId);
    
    /**
     * 获取特定课程的书籍推荐
     * @param courseId 课程ID
     * @param userId 学生ID
     * @return 推荐书籍列表
     */
    List<AIBookRecommendationVO> getRecommendationsByCourse(Long courseId, Long userId);
    
    /**
     * 标记推荐为已读
     * @param recommendationId 推荐ID
     * @param userId 学生ID
     * @return 是否成功
     */
    boolean markAsRead(Long recommendationId, Long userId);
    
    /**
     * 删除推荐记录
     * @param recommendationId 推荐记录ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteRecommendation(Long recommendationId, Long userId);
} 