package com.csu.sms.persistence;

import com.csu.sms.model.AIBookRecommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AIBookRecommendationMapper {
    
    int insert(AIBookRecommendation recommendation);
    
    int updateIsRead(@Param("id") Long id, @Param("userId") Long userId);
    
    List<AIBookRecommendation> findByUserId(@Param("userId") Long userId);
    
    AIBookRecommendation findById(@Param("id") Long id);
    
    List<AIBookRecommendation> findByCourseIdAndUserId(@Param("courseId") Long courseId, @Param("userId") Long userId);
    
    /**
     * 删除推荐记录
     * @param id 推荐记录ID
     * @param userId 用户ID（确保只能删除自己的推荐）
     * @return 删除的记录数
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
} 