package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author CSU Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理用户上下文异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<String> handleIllegalStateException(IllegalStateException e) {
        if (e.getMessage() != null && e.getMessage().contains("当前用户ID不存在")) {
            return ApiResponse.error(401, "请先登录");
        }
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        return ApiResponse.error(500, "服务器内部错误：" + e.getMessage());
    }
} 