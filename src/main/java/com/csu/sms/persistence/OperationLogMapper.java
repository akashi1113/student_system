package com.csu.sms.persistence;

import com.csu.sms.model.OperationLog;
import com.csu.sms.vo.OperationLogReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper {
    
    /**
     * 插入操作日志
     */
    int insert(OperationLog operationLog);
    
    /**
     * 根据条件查询操作日志
     */
    List<OperationLog> selectByCondition(@Param("userId") Long userId,
                                        @Param("module") String module,
                                        @Param("operation") String operation,
                                        @Param("status") String status,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);
    
    /**
     * 统计操作日志数量
     */
    long countByCondition(@Param("userId") Long userId,
                         @Param("module") String module,
                         @Param("operation") String operation,
                         @Param("status") String status,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);
    
    /**
     * 删除指定日期之前的日志
     */
    int deleteBeforeDate(@Param("date") String date);

    List<OperationLogReportVO> auditReport(
        @Param("userId") Long userId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("operation") String operation,
        @Param("module") String module,
        @Param("offset") int offset,
        @Param("size") int size
    );

    int auditReportCount(
        @Param("userId") Long userId,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("operation") String operation,
        @Param("module") String module
    );

    /**
     * 查询分组下所有原始操作日志明细
     */
    List<OperationLog> selectDetails(@Param("username") String username,
                                     @Param("module") String module,
                                     @Param("operation") String operation,
                                     @Param("startTime") String startTime,
                                     @Param("endTime") String endTime,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 统计分组下所有原始操作日志明细数量
     */
    long countDetails(@Param("username") String username,
                      @Param("module") String module,
                      @Param("operation") String operation,
                      @Param("startTime") String startTime,
                      @Param("endTime") String endTime);
} 