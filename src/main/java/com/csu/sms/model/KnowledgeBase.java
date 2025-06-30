package com.csu.sms.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库实体类
 * @author CSU Team
 */
@Data
public class KnowledgeBase {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 书名
     */
    private String title;

    /**
     * 作者
     */
    private String author;

    /**
     * 简介描述
     */
    private String content;

    /**
     * 图片路径
     */
    private String imagePath;

    /**
     * 外部链接地址
     */
    private String linkUrl;

    /**
     * 分类标签
     */
    private String category;

    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}