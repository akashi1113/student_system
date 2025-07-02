package com.csu.sms.service;

import com.csu.sms.model.KnowledgeBase;
import com.csu.sms.persistence.KnowledgeBaseMapper;
import com.csu.sms.common.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 知识库业务逻辑接口
 * @author CSU Team
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KnowledgeBaseService {
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Transactional(readOnly = true)
    public PageResult<KnowledgeBase> getKnowledgeList(String keyword, String category, Integer current, Integer size) {
        current = current != null && current > 0 ? current : 1;
        size = size != null && size > 0 ? size : 10;
        int offset = (current - 1) * size;
        List<KnowledgeBase> records = knowledgeBaseMapper.selectKnowledgePage(keyword, category, offset, size);
        Long total = knowledgeBaseMapper.countKnowledge(keyword, category);
        return PageResult.of(records, total, current, size);
    }

    @Transactional(readOnly = true)
    public KnowledgeBase getKnowledgeDetail(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID不能为空或小于等于0");
        }
        return knowledgeBaseMapper.selectById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return knowledgeBaseMapper.selectAllCategories();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCategoryStatistics() {
        return knowledgeBaseMapper.countByCategory();
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBase> getPopularBooks(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        return knowledgeBaseMapper.selectPopularBooks(limit);
    }

    public boolean addKnowledge(KnowledgeBase knowledgeBase) {
        validateKnowledgeBase(knowledgeBase);
        knowledgeBase.setCreateTime(LocalDateTime.now());
        knowledgeBase.setUpdateTime(LocalDateTime.now());
        knowledgeBase.setStatus(1);
        return knowledgeBaseMapper.insert(knowledgeBase) > 0;
    }

    public boolean batchAddKnowledge(List<KnowledgeBase> knowledgeList) {
        if (knowledgeList == null || knowledgeList.isEmpty()) {
            throw new IllegalArgumentException("知识库列表不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        knowledgeList.forEach(knowledge -> {
            validateKnowledgeBase(knowledge);
            knowledge.setCreateTime(now);
            knowledge.setUpdateTime(now);
            knowledge.setStatus(1);
        });
        return knowledgeBaseMapper.batchInsert(knowledgeList) > 0;
    }

    public boolean updateKnowledge(KnowledgeBase knowledgeBase) {
        if (knowledgeBase.getId() == null) {
            throw new IllegalArgumentException("更新记录时ID不能为空");
        }
        validateKnowledgeBase(knowledgeBase);
        knowledgeBase.setUpdateTime(LocalDateTime.now());
        return knowledgeBaseMapper.updateById(knowledgeBase) > 0;
    }

    public boolean deleteKnowledge(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID不能为空或小于等于0");
        }
        KnowledgeBase existing = knowledgeBaseMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("要删除的记录不存在");
        }
        return knowledgeBaseMapper.deleteById(id) > 0;
    }

    private void validateKnowledgeBase(KnowledgeBase knowledgeBase) {
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("知识库信息不能为空");
        }
        if (knowledgeBase.getTitle() == null || knowledgeBase.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("书名不能为空");
        }
        if (knowledgeBase.getTitle().length() > 255) {
            throw new IllegalArgumentException("书名长度不能超过255个字符");
        }
        if (knowledgeBase.getAuthor() != null && knowledgeBase.getAuthor().length() > 100) {
            throw new IllegalArgumentException("作者名长度不能超过100个字符");
        }
        if (knowledgeBase.getCategory() != null && knowledgeBase.getCategory().length() > 50) {
            throw new IllegalArgumentException("分类长度不能超过50个字符");
        }
    }
}