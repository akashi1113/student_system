package com.csu.sms.controller;

import com.csu.sms.model.KnowledgeBase;
import com.csu.sms.service.KnowledgeBaseService;
import com.csu.sms.util.PageResult;
import com.csu.sms.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 * @author CSU Team
 */
@CrossOrigin(origins = {"http://localhost:5173"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
        allowCredentials = "true")
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 分页查询知识库列表
     * @param keyword 搜索关键词
     * @param category 分类标签
     * @param current 当前页码（默认1）
     * @param size 每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/books")
    public ApiResponse<PageResult<KnowledgeBase>> getKnowledgeList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageResult<KnowledgeBase> result = knowledgeBaseService.getKnowledgeList(keyword, category, current, size);
            return ApiResponse.success("查询成功", result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取知识库详情
     * @param id 知识库ID
     * @return 知识库详情
     */
    @GetMapping("/book/detail/{id}")
    public ApiResponse<KnowledgeBase> getKnowledgeDetail(@PathVariable Long id) {
        try {
            KnowledgeBase knowledge = knowledgeBaseService.getKnowledgeDetail(id);
            if (knowledge == null) {
                return ApiResponse.error("知识库资源不存在", "404");
            }
            return ApiResponse.success("查询成功", knowledge);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有分类
     * @return 分类列表
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        try {
            List<String> categories = knowledgeBaseService.getAllCategories();
            return ApiResponse.success("查询成功", categories);
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取分类统计
     * @return 分类统计数据
     */
    @GetMapping("/categories/statistics")
    public ApiResponse<List<Map<String, Object>>> getCategoryStatistics() {
        try {
            List<Map<String, Object>> statistics = knowledgeBaseService.getCategoryStatistics();
            return ApiResponse.success("查询成功", statistics);
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门书籍
     * @param limit 限制数量（默认10）
     * @return 热门书籍列表
     */
    @GetMapping("/books/popular")
    public ApiResponse<List<KnowledgeBase>> getPopularBooks(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<KnowledgeBase> books = knowledgeBaseService.getPopularBooks(limit);
            return ApiResponse.success("查询成功", books);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加知识库
     * @param knowledgeBase 知识库信息
     * @return 操作结果
     */
    @PostMapping("/book")
    public ApiResponse<String> addKnowledge(@Valid @RequestBody KnowledgeBase knowledgeBase) {
        try {
            boolean success = knowledgeBaseService.addKnowledge(knowledgeBase);
            if (success) {
                return ApiResponse.success("添加成功", null);
            } else {
                return ApiResponse.error("添加失败", "500");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 批量添加知识库
     * @param knowledgeList 知识库列表
     * @return 操作结果
     */
    @PostMapping("/books/batch")
    public ApiResponse<String> batchAddKnowledge(@Valid @RequestBody List<KnowledgeBase> knowledgeList) {
        try {
            boolean success = knowledgeBaseService.batchAddKnowledge(knowledgeList);
            if (success) {
                return ApiResponse.success("批量添加成功", null);
            } else {
                return ApiResponse.error("批量添加失败", "500");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("批量添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新知识库
     * @param knowledgeBase 知识库信息
     * @return 操作结果
     */
    @PutMapping("/book")
    public ApiResponse<String> updateKnowledge(@Valid @RequestBody KnowledgeBase knowledgeBase) {
        try {
            boolean success = knowledgeBaseService.updateKnowledge(knowledgeBase);
            if (success) {
                return ApiResponse.success("更新成功", null);
            } else {
                return ApiResponse.error("更新失败", "500");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除知识库
     * @param id 知识库ID
     * @return 操作结果
     */
    @DeleteMapping("/book/{id}")
    public ApiResponse<String> deleteKnowledge(@PathVariable Long id) {
        try {
            boolean success = knowledgeBaseService.deleteKnowledge(id);
            if (success) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error("删除失败，可能资源不存在", "404");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage(), "400");
        } catch (Exception e) {
            return ApiResponse.error("删除失败：" + e.getMessage());
        }
    }
}