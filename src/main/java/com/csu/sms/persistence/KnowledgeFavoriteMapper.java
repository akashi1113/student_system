package com.csu.sms.persistence;

import com.csu.sms.model.knowledge.KnowledgeFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeFavoriteMapper {
    int insertFavorite(KnowledgeFavorite favorite);
    int updateFavoriteStatus(@Param("userId") Long userId, @Param("knowledgeId") Long knowledgeId, @Param("status") Integer status);
    KnowledgeFavorite selectByUserAndKnowledge(@Param("userId") Long userId, @Param("knowledgeId") Long knowledgeId);
    List<KnowledgeFavorite> selectByUserId(@Param("userId") Long userId);
} 