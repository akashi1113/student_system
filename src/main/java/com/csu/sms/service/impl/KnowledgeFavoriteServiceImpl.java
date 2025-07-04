package com.csu.sms.service.impl;

import com.csu.sms.model.knowledge.KnowledgeFavorite;
import com.csu.sms.persistence.KnowledgeFavoriteMapper;
import com.csu.sms.service.KnowledgeFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class KnowledgeFavoriteServiceImpl implements KnowledgeFavoriteService {
    @Autowired
    private KnowledgeFavoriteMapper favoriteMapper;

    @Override
    public boolean addFavorite(Long userId, Long knowledgeId, String remark) {
        KnowledgeFavorite existing = favoriteMapper.selectByUserAndKnowledge(userId, knowledgeId);
        Date now = new Date();
        if (existing == null) {
            KnowledgeFavorite favorite = new KnowledgeFavorite();
            favorite.setUserId(userId);
            favorite.setKnowledgeId(knowledgeId);
            favorite.setRemark(remark);
            favorite.setFavoriteTime(now);
            favorite.setStatus(1);
            return favoriteMapper.insertFavorite(favorite) > 0;
        } else if (existing.getStatus() == 0) {
            // 恢复收藏
            existing.setStatus(1);
            existing.setRemark(remark);
            existing.setFavoriteTime(now);
            return favoriteMapper.updateFavoriteStatus(userId, knowledgeId, 1) > 0;
        } else {
            // 已收藏，更新备注
            existing.setRemark(remark);
            return favoriteMapper.updateFavoriteStatus(userId, knowledgeId, 1) > 0;
        }
    }

    @Override
    public boolean cancelFavorite(Long userId, Long knowledgeId) {
        return favoriteMapper.updateFavoriteStatus(userId, knowledgeId, 0) > 0;
    }

    @Override
    public boolean isFavorite(Long userId, Long knowledgeId) {
        KnowledgeFavorite favorite = favoriteMapper.selectByUserAndKnowledge(userId, knowledgeId);
        return favorite != null && favorite.getStatus() == 1;
    }

    @Override
    public List<KnowledgeFavorite> getUserFavorites(Long userId) {
        return favoriteMapper.selectByUserId(userId);
    }
} 