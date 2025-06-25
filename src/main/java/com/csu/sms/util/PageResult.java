package com.csu.sms.util;

import lombok.Data;
import java.util.List;

/**
 * 分页结果类
 * @author CSU Team
 */
@Data
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Long pages;

    public PageResult() {}

    public PageResult(List<T> records, Long total, Integer current, Integer size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = total > 0 ? (total + size - 1) / size : 0;
    }

    /**
     * 计算偏移量
     */
    public static Integer calculateOffset(Integer current, Integer size) {
        current = current != null && current > 0 ? current : 1;
        size = size != null && size > 0 ? size : 10;
        return (current - 1) * size;
    }
}