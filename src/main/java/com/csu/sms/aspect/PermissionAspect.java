package com.csu.sms.aspect;

import com.csu.sms.annotation.RequireAdmin;
import com.csu.sms.annotation.RequireTeacher;
import com.csu.sms.common.ServiceException;
import com.csu.sms.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 权限检查切面
 */
@Aspect
@Component
@Slf4j
public class PermissionAspect {

    /**
     * 检查管理员权限
     */
    @Before("@annotation(requireAdmin)")
    public void checkAdminPermission(JoinPoint joinPoint, RequireAdmin requireAdmin) {
        if (!UserContext.isAdmin()) {
            log.warn("用户 {} 尝试访问管理员接口: {}", 
                UserContext.getCurrentUserId(), 
                joinPoint.getSignature().getName());
            throw ServiceException.permissionDenied(requireAdmin.message());
        }
    }

    /**
     * 检查教师权限
     */
    @Before("@annotation(requireTeacher)")
    public void checkTeacherPermission(JoinPoint joinPoint, RequireTeacher requireTeacher) {
        if (!UserContext.isTeacher()) {
            log.warn("用户 {} 尝试访问教师接口: {}", 
                UserContext.getCurrentUserId(), 
                joinPoint.getSignature().getName());
            throw ServiceException.permissionDenied(requireTeacher.message());
        }
    }
} 