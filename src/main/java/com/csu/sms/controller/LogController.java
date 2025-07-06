package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.model.OperationLog;
import com.csu.sms.model.SystemLog;
import com.csu.sms.service.LogService;
import com.csu.sms.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 日志管理控制器
 */
@RestController
@RequestMapping("/api/logs")
@Slf4j
public class LogController {
    
    @Autowired
    private LogService logService;
    
    /**
     * 分页查询操作日志
     */
    @GetMapping("/operation")
    public ApiResponse<PageResult<OperationLog>> getOperationLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 权限检查：只有管理员可以查看日志
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以查看日志", "PERMISSION_DENIED");
        }
        
        try {
            PageResult<OperationLog> result = logService.getOperationLogs(
                userId, module, operation, status, startTime, endTime, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询操作日志失败", e);
            return ApiResponse.error("查询操作日志失败");
        }
    }
    
    /**
     * 分页查询系统日志
     */
    @GetMapping("/system")
    public ApiResponse<PageResult<SystemLog>> getSystemLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 权限检查：只有管理员可以查看日志
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以查看日志", "PERMISSION_DENIED");
        }
        
        try {
            PageResult<SystemLog> result = logService.getSystemLogs(
                level, type, source, startTime, endTime, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("查询系统日志失败", e);
            return ApiResponse.error("查询系统日志失败");
        }
    }
    
    /**
     * 清理过期日志
     */
    @DeleteMapping("/cleanup")
    public ApiResponse<String> cleanupLogs(@RequestParam(defaultValue = "30") int days) {
        // 权限检查：只有管理员可以清理日志
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以清理日志", "PERMISSION_DENIED");
        }
        
        try {
            logService.cleanupExpiredLogs(days);
            return ApiResponse.success("清理过期日志成功");
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
            return ApiResponse.error("清理过期日志失败");
        }
    }
} 