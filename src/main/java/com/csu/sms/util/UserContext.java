package com.csu.sms.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户的信息
 */
@Slf4j
@Component
public class UserContext {
    
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ID_PARAM = "userId";
    private static final String USERNAME_HEADER = "X-Username";
    private static final String USERNAME_PARAM = "username";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    // JWT密钥（应与JwtUtil中的一致）
    private static final String SECRET = "6v9y$B&E)H@McQfTjWnZr4u7x!A%D*G-";

    // 新增：ThreadLocal方式手动设置用户信息
    private static final ThreadLocal<Long> threadLocalUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalUsername = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalLoginType = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        threadLocalUserId.set(userId);
    }
    public static void setCurrentUsername(String username) {
        threadLocalUsername.set(username);
    }
    public static void setLoginType(String loginType) {
        threadLocalLoginType.set(loginType);
    }
    public static String getLoginType() {
        return threadLocalLoginType.get();
    }
    public static void clear() {
        threadLocalUserId.remove();
        threadLocalUsername.remove();
        threadLocalLoginType.remove();
    }

    // 生成密钥
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从请求头获取JWT token
     */
    private static String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 从token中提取用户ID
     */
    private static Long extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("user_id", Long.class);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取当前用户ID
     * 优先从JWT token获取，其次从请求头/参数获取
     */
    public static Long getCurrentUserId() {
        Long threadId = threadLocalUserId.get();
        if (threadId != null) return threadId;
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        
        HttpServletRequest request = attributes.getRequest();
        String token = getTokenFromRequest(request);

        if (token != null) {
            Long userId = extractUserIdFromToken(token);
            if (userId != null) {
                return userId;
            }
        }
        
        // 优先从请求头获取
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
            try {
                return Long.parseLong(userIdHeader.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in header: {}", userIdHeader);
            }
        }
        
        // 从请求参数获取
        String userIdParam = request.getParameter(USER_ID_PARAM);
        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            try {
                return Long.parseLong(userIdParam.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID in parameter: {}", userIdParam);
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
        String threadName = threadLocalUsername.get();
        if (threadName != null) return threadName;
        
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