package com.csu.sms.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户的信息
 */
public class UserContext {
    
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ID_PARAM = "userId";
    private static final String USERNAME_HEADER = "X-Username";
    private static final String USERNAME_PARAM = "username";
    
    /**
     * 获取当前用户ID
     * 优先从请求头获取，其次从请求参数获取
     */
    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 优先从请求头获取
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(userIdHeader.trim());
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }
        }
        
        // 从请求参数获取
        String userIdParam = request.getParameter(USER_ID_PARAM);
        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            try {
                return Long.parseLong(userIdParam.trim());
            } catch (NumberFormatException e) {
                // 忽略格式错误
            }
        }
        
        return null;
    }
    
    /**
     * 检查是否有有效的用户ID
     */
    public static boolean hasValidUserId() {
        return getCurrentUserId() != null;
    }
    
    /**
     * 获取当前用户ID，如果不存在则抛出异常
     */
    public static Long getRequiredCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("当前用户ID不存在，请先登录");
        }
        return userId;
    }
    
    /**
     * 获取当前用户名
     * 优先从请求头获取，其次从请求参数获取
     */
    public static String getCurrentUsername() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 优先从请求头获取
        String usernameHeader = request.getHeader(USERNAME_HEADER);
        if (usernameHeader != null && !usernameHeader.trim().isEmpty()) {
            return usernameHeader.trim();
        }
        
        // 从请求参数获取
        String usernameParam = request.getParameter(USERNAME_PARAM);
        if (usernameParam != null && !usernameParam.trim().isEmpty()) {
            return usernameParam.trim();
        }
        
        return null;
    }
} 