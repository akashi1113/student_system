package com.csu.sms.service;

import com.csu.sms.domain.KnowledgeBase;
import com.csu.sms.util.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 知识库业务逻辑接口
 * @author CSU Team
 */
public interface KnowledgeBaseService {

    /**
     * 分页查询知识库列表
     * @param keyword 搜索关键词
     * @param category 分类标签
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<KnowledgeBase> getKnowledgeList(String keyword, String category, Integer current, Integer size);

    /**
     * 根据ID获取知识库详情
     */
    KnowledgeBase getKnowledgeDetail(Long id);

    /**
     * 获取所有分类
     */
    List<String> getAllCategories();

    /**
     * 按分类统计数量
     */
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 获取热门书籍
     */
    List<KnowledgeBase> getPopularBooks(Integer limit);

    /**
     * 添加知识库
     */
    boolean addKnowledge(KnowledgeBase knowledgeBase);

    /**
     * 批量添加知识库
     */
    boolean batchAddKnowledge(List<KnowledgeBase> knowledgeList);

    /**
     * 更新知识库
     */
    boolean updateKnowledge(KnowledgeBase knowledgeBase);

    /**
     * 删除知识库
     */
    boolean deleteKnowledge(Long id);
}