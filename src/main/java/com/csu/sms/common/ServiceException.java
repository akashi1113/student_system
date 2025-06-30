package com.csu.sms.common;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
    private final int code; // 错误码
    private final String message; // 错误信息

    public ServiceException(int code, String message) {
        super(message); // 调用父类的构造器，将message传给它
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message) {
        this(500, message); // 默认错误码为500
    }
}
