package com.csu.sms.common;

import lombok.Getter;

/**
 * 服务异常类
 */
@Getter
public class ServiceException extends RuntimeException {
    private final int code; // 错误码
    private final String message; // 错误信息
    private String errorCode;

    public ServiceException(int code, String message) {
        super(message); // 调用父类的构造器，将message传给它
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message) {
        this(500, message); // 默认错误码为500
    }

    public ServiceException(int code, String message, String errorCode) {
        super(message);
        this.code = code;
        this.message = message;
        this.errorCode = errorCode;
    }

    public int getCode() {
        return code;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 创建权限不足异常
     */
    public static ServiceException permissionDenied(String message) {
        return new ServiceException(403, message, "PERMISSION_DENIED");
    }

    /**
     * 创建未登录异常
     */
    public static ServiceException notLoggedIn() {
        return new ServiceException(401, "用户未登录", "USER_NOT_LOGGED_IN");
    }

    /**
     * 创建资源不存在异常
     */
    public static ServiceException notFound(String message) {
        return new ServiceException(404, message, "RESOURCE_NOT_FOUND");
    }

    /**
     * 创建参数错误异常
     */
    public static ServiceException badRequest(String message) {
        return new ServiceException(400, message, "BAD_REQUEST");
    }
}
