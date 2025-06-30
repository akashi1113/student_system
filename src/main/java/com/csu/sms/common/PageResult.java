package com.csu.sms.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;//总记录数
    private int pageNum;//当前页码
    private int pageSize;//每页记录数
    private int pages;//总页数

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);//当前页的数据列表
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) Math.ceil((double) total / pageSize));//向上取整
        return result;
    }
}
