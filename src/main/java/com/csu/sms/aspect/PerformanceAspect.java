package com.csu.sms.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 * 记录接口执行时间
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录性能日志
            log.info("接口性能监控 - 方法: {}, 执行时间: {}ms", 
                    joinPoint.getSignature().getName(), executionTime);
            
            // 如果执行时间超过1秒，记录警告
            if (executionTime > 1000) {
                log.warn("接口执行时间过长 - 方法: {}, 执行时间: {}ms", 
                        joinPoint.getSignature().getName(), executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("接口执行异常 - 方法: {}, 执行时间: {}ms, 异常: {}", 
                    joinPoint.getSignature().getName(), executionTime, e.getMessage());
            throw e;
        }
    }
} 