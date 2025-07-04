package com.csu.sms.service;

import com.csu.sms.model.OperationLog;
import com.csu.sms.model.SystemLog;
import com.csu.sms.common.PageResult;
import com.csu.sms.vo.OperationLogReportVO;

/**
 * 日志服务接口
 */
public interface LogService {
    
    /**
     * 记录操作日志
     */
    void recordOperationLog(OperationLog operationLog);
    
    /**
     * 记录系统日志
     */
    void recordSystemLog(SystemLog systemLog);
    
    /**
     * 分页查询操作日志
     */
    PageResult<OperationLog> getOperationLogs(Long userId, String module, String operation, 
                                             String status, String startTime, String endTime, 
                                             int page, int size);
    
    /**
     * 分页查询系统日志
     */
    PageResult<SystemLog> getSystemLogs(String level, String type, String source, 
                                       String startTime, String endTime, int page, int size);
    
    /**
     * 清理过期日志
     */
    void cleanupExpiredLogs(int days);

    /**
     * 获取操作审计报表
     * @param userId 用户ID，可选
     * @param username 用户名，可选
     * @param startTime 开始时间，可选
     * @param endTime 结束时间，可选
     * @param operation 操作类型，可选
     * @param module 模块名称，可选
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<OperationLogReportVO> getOperationAuditReport(Long userId, String username, String startTime, String endTime, String operation, String module, int page, int size);

    /**
     * 分组明细接口：根据分组条件查询原始操作日志明细
     */
    PageResult<OperationLog> getOperationLogDetails(String username, String module, String operation, String startTime, String endTime, int page, int size);
} 