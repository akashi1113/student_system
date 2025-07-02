package com.csu.sms.aspect;

import com.csu.sms.annotation.LogOperation;
import com.csu.sms.model.OperationLog;
import com.csu.sms.service.LogService;
import com.csu.sms.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 日志切面
 * 自动记录带有@LogOperation注解的方法调用
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    
    @Autowired
    private LogService logService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Around("@annotation(com.csu.sms.annotation.LogOperation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String status = "SUCCESS";
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = "FAILED";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            try {
                recordOperationLog(joinPoint, result, status, errorMessage, 
                                 System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }
    
    private void recordOperationLog(ProceedingJoinPoint joinPoint, Object result, 
                                   String status, String errorMessage, long executionTime) {
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
            
            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation logOperation = method.getAnnotation(LogOperation.class);
            
            // 创建操作日志
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(UserContext.getCurrentUserId());
            operationLog.setUsername(UserContext.getCurrentUsername());
            operationLog.setModule(logOperation.module());
            operationLog.setOperation(logOperation.operation());
            operationLog.setDescription(logOperation.description());
            operationLog.setStatus(status);
            operationLog.setExecutionTime(executionTime);
            operationLog.setCreateTime(LocalDateTime.now());
            
            // 设置请求信息
            if (request != null) {
                operationLog.setRequestUrl(request.getRequestURI());
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setIpAddress(getClientIpAddress(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));
                
                // 记录请求参数（简化处理）
                try {
                    String params = objectMapper.writeValueAsString(joinPoint.getArgs());
                    operationLog.setRequestParams(params.length() > 1000 ? 
                        params.substring(0, 1000) + "..." : params);
                } catch (Exception e) {
                    operationLog.setRequestParams("参数序列化失败");
                }
            }
            
            // 记录响应结果（简化处理）
            if (result != null) {
                try {
                    String response = objectMapper.writeValueAsString(result);
                    operationLog.setResponseResult(response.length() > 1000 ? 
                        response.substring(0, 1000) + "..." : response);
                } catch (Exception e) {
                    operationLog.setResponseResult("响应序列化失败");
                }
            }
            
            // 设置错误信息
            if (errorMessage != null) {
                operationLog.setErrorMessage(errorMessage.length() > 500 ? 
                    errorMessage.substring(0, 500) + "..." : errorMessage);
            }
            
            // 记录到文件日志
            log.info("操作日志记录 - 模块: {}, 操作: {}, 用户: {}, 状态: {}, 执行时间: {}ms", 
                    operationLog.getModule(), operationLog.getOperation(), 
                    operationLog.getUsername(), operationLog.getStatus(), executionTime);
            
            // 异步记录日志到数据库
            logService.recordOperationLog(operationLog);
            
        } catch (Exception e) {
            log.error("构建操作日志失败", e);
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 