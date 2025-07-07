package com.csu.sms.service;

import com.csu.sms.model.knowledge.KnowledgeFavorite;
import java.util.List;

public interface KnowledgeFavoriteService {
    boolean addFavorite(Long userId, Long knowledgeId, String remark);
    boolean cancelFavorite(Long userId, Long knowledgeId);
    boolean isFavorite(Long userId, Long knowledgeId);
    List<KnowledgeFavorite> getUserFavorites(Long userId);
} 