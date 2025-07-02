package com.csu.sms.common;

import com.csu.sms.dto.ApiResponse;
import com.csu.sms.model.SystemLog;
import com.csu.sms.service.LogService;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private LogService logService;

    //处理参数验证异常
    @ExceptionHandler(BindException.class)
    public ApiResponse<Map<String, String>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        recordSystemExceptionLog("参数验证失败", e);
        return ApiResponse.error("参数验证失败", "VALIDATION_ERROR");
    }

    //处理约束验证异常
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<String> handleConstraintViolationException(ConstraintViolationException e) {
        recordSystemExceptionLog("参数验证失败", e);
        return ApiResponse.error("参数验证失败: " + e.getMessage(), "VALIDATION_ERROR");
    }

    //处理运行时异常
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<String> handleRuntimeException(RuntimeException e) {
        recordSystemExceptionLog("运行时异常", e);
        return ApiResponse.error("操作失败: " + e.getMessage(), "RUNTIME_ERROR");
    }

    //处理通用异常
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        recordSystemExceptionLog("系统异常", e);
        return ApiResponse.error("系统异常: " + e.getMessage(), "SYSTEM_ERROR");
    }

    /**
     * 处理用户上下文异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public com.csu.sms.common.ApiResponse<String> handleIllegalStateException(IllegalStateException e) {
        recordSystemExceptionLog("用户上下文异常", e);
        if (e.getMessage() != null && e.getMessage().contains("当前用户ID不存在")) {
            return com.csu.sms.common.ApiResponse.error(401, "请先登录");
        }
        return com.csu.sms.common.ApiResponse.error(400, e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public com.csu.sms.common.ApiResponse<String> handleIllegalArgumentException(IllegalArgumentException e) {
        recordSystemExceptionLog("参数异常", e);
        return com.csu.sms.common.ApiResponse.error(400, e.getMessage());
    }

    private void recordSystemExceptionLog(String title, Exception e) {
        try {
            SystemLog log = new SystemLog();
            log.setLevel("ERROR");
            log.setType("系统异常");
            log.setTitle(title);
            log.setContent(e.getMessage());
            log.setSource("GlobalExceptionHandler");
            log.setStackTrace(getStackTraceString(e));
            logService.recordSystemLog(log);
        } catch (Exception ignore) {}
    }

    private String getStackTraceString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}