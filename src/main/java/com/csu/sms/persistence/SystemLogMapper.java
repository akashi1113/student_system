package com.csu.sms.persistence;

import com.csu.sms.model.SystemLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统日志Mapper接口
 */
@Mapper
public interface SystemLogMapper {
    
    /**
     * 插入系统日志
     */
    int insert(SystemLog systemLog);
    
    /**
     * 根据条件查询系统日志
     */
    List<SystemLog> selectByCondition(@Param("level") String level,
                                     @Param("type") String type,
                                     @Param("source") String source,
                                     @Param("startTime") String startTime,
                                     @Param("endTime") String endTime,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
    
    /**
     * 统计系统日志数量
     */
    long countByCondition(@Param("level") String level,
                         @Param("type") String type,
                         @Param("source") String source,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);
    
    /**
     * 删除指定日期之前的日志
     */
    int deleteBeforeDate(@Param("date") String date);
} 