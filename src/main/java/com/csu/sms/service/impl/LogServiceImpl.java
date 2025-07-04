package com.csu.sms.service.impl;

import com.csu.sms.model.OperationLog;
import com.csu.sms.model.SystemLog;
import com.csu.sms.persistence.OperationLogMapper;
import com.csu.sms.persistence.SystemLogMapper;
import com.csu.sms.service.LogService;
import com.csu.sms.common.PageResult;
import com.csu.sms.vo.OperationLogReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 日志服务实现类
 */
@Service
@Slf4j
public class LogServiceImpl implements LogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Autowired
    private SystemLogMapper systemLogMapper;
    
    @Override
    @Async("logTaskExecutor")
    public void recordOperationLog(OperationLog operationLog) {
        try {
            if (operationLog.getCreateTime() == null) {
                operationLog.setCreateTime(LocalDateTime.now());
            }
            operationLogMapper.insert(operationLog);
            
            // 记录到文件日志
            log.info("操作日志已保存到数据库 - 模块: {}, 操作: {}, 用户: {}", 
                    operationLog.getModule(), operationLog.getOperation(), operationLog.getUsername());
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }
    
    @Override
    @Async("logTaskExecutor")
    public void recordSystemLog(SystemLog systemLog) {
        try {
            if (systemLog.getCreateTime() == null) {
                systemLog.setCreateTime(LocalDateTime.now());
            }
            systemLogMapper.insert(systemLog);
            
            // 记录到文件日志
            log.info("系统日志已保存到数据库 - 级别: {}, 类型: {}, 标题: {}", 
                    systemLog.getLevel(), systemLog.getType(), systemLog.getTitle());
        } catch (Exception e) {
            log.error("保存系统日志失败", e);
        }
    }
    
    @Override
    public PageResult<OperationLog> getOperationLogs(Long userId, String module, String operation, 
                                                    String status, String startTime, String endTime, 
                                                    int page, int size) {
        int offset = (page - 1) * size;
        
        List<OperationLog> logs = operationLogMapper.selectByCondition(
            userId, module, operation, status, startTime, endTime, offset, size);
        
        long total = operationLogMapper.countByCondition(
            userId, module, operation, status, startTime, endTime);
        
        return PageResult.of(logs, total, page, size);
    }
    
    @Override
    public PageResult<SystemLog> getSystemLogs(String level, String type, String source, 
                                              String startTime, String endTime, int page, int size) {
        int offset = (page - 1) * size;
        
        List<SystemLog> logs = systemLogMapper.selectByCondition(
            level, type, source, startTime, endTime, offset, size);
        
        long total = systemLogMapper.countByCondition(
            level, type, source, startTime, endTime);
        
        return PageResult.of(logs, total, page, size);
    }
    
    @Override
    public void cleanupExpiredLogs(int days) {
        try {
            LocalDateTime expireDate = LocalDateTime.now().minusDays(days);
            String dateStr = expireDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            int operationLogCount = operationLogMapper.deleteBeforeDate(dateStr);
            int systemLogCount = systemLogMapper.deleteBeforeDate(dateStr);
            
            log.info("清理过期日志完成，操作日志：{}条，系统日志：{}条", operationLogCount, systemLogCount);
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
        }
    }
    
    @Override
    public PageResult<OperationLogReportVO> getOperationAuditReport(Long userId, String username, String startTime, String endTime, String operation, String module, int page, int size) {
        int offset = (page - 1) * size;
        
        // 添加调试日志
        log.info("审计报表查询参数 - userId: {}, username: {}, startTime: {}, endTime: {}, operation: {}, module: {}, page: {}, size: {}", 
                userId, username, startTime, endTime, operation, module, page, size);
        
        List<OperationLogReportVO> records = operationLogMapper.auditReport(userId, username, startTime, endTime, operation, module, offset, size);
        int total = operationLogMapper.auditReportCount(userId, username, startTime, endTime, operation, module);
        
        // 添加结果日志
        log.info("审计报表查询结果 - 记录数: {}, 总数: {}", records.size(), total);
        records.forEach(record -> log.info("记录: module={}, operation={}, count={}", record.getModule(), record.getOperation(), record.getCount()));
        
        return PageResult.of(records, total, page, size);
    }

    @Override
    public PageResult<OperationLog> getOperationLogDetails(String username, String module, String operation, String startTime, String endTime, int page, int size) {
        int offset = (page - 1) * size;
        java.util.List<OperationLog> logs = operationLogMapper.selectDetails(username, module, operation, startTime, endTime, offset, size);
        long total = operationLogMapper.countDetails(username, module, operation, startTime, endTime);
        return PageResult.of(logs, total, page, size);
    }
} 