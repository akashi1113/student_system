package com.csu.sms.persistence;

import com.csu.sms.model.KnowledgeBase;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 知识库数据访问层
 * @author CSU Team
 */
@Repository
public interface KnowledgeBaseMapper {

    /**
     * 根据ID查询知识库详情
     */
    KnowledgeBase selectById(@Param("id") Long id);

    /**
     * 分页查询知识库列表
     */
    List<KnowledgeBase> selectKnowledgePage(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 查询知识库总数
     */
    Long countKnowledge(
            @Param("keyword") String keyword,
            @Param("category") String category
    );

    /**
     * 插入知识库记录
     */
    int insert(KnowledgeBase knowledgeBase);

    /**
     * 批量插入知识库记录
     */
    int batchInsert(@Param("list") List<KnowledgeBase> knowledgeList);

    /**
     * 更新知识库记录
     */
    int updateById(KnowledgeBase knowledgeBase);

    /**
     * 删除知识库记录（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 获取所有分类
     */
    List<String> selectAllCategories();

    /**
     * 按分类统计数量
     */
    List<Map<String, Object>> countByCategory();

    /**
     * 查询热门书籍
     */
    List<KnowledgeBase> selectPopularBooks(@Param("limit") Integer limit);

    /**
     * 查询知识库列表
     */
    List<KnowledgeBase> selectKnowledgeList(@Param("keyword") String keyword, @Param("category") String category, @Param("offset") int offset, @Param("size") int size);

    /**
     * 查询知识库列表总数
     */
    int countKnowledgeList(@Param("keyword") String keyword, @Param("category") String category);
}