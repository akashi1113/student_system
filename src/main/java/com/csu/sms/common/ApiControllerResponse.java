package com.csu.sms.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiControllerResponse<T> {
    private int code; // 状态码，200 成功，4xx 客户端错误，5xx 服务器错误
    private String message; // 消息
    private T data; // 数据

    // 成功响应
    public static <T> ApiControllerResponse<T> success(T data) {
        return new ApiControllerResponse<>(200, "操作成功", data);
    }

    public static <T> ApiControllerResponse<T> success(String message, T data) {
        return new ApiControllerResponse<>(200, message, data);
    }

    public static <T> ApiControllerResponse<T> success(String message) {
        return new ApiControllerResponse<>(200, message, null);
    }

    // 失败响应
    public static <T> ApiControllerResponse<T> error(int code, String message) {
        return new ApiControllerResponse<>(code, message, null);
    }

    // 默认的失败响应
    public static <T> ApiControllerResponse<T> error(String message) {
        return new ApiControllerResponse<>(500, message, null); // 默认500
    }
}
