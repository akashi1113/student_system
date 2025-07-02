package com.csu.sms.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // 确保这个构造函数包含了所有字段
public class ApiControllerResponse<T> {
    private int code; // 状态码，200 成功，4xx 客户端错误，5xx 服务器错误
    private String message; // 消息
    private Boolean success; // <--- 新增：表示业务操作是否成功
    private T data; // 数据

    // 成功响应 (推荐使用这个，明确设置 success = true)
    public static <T> ApiControllerResponse<T> success(T data) {
        return new ApiControllerResponse<>(200, "操作成功", true, data);
    }

    public static <T> ApiControllerResponse<T> success(String message, T data) {
        return new ApiControllerResponse<>(200, message, true, data); // 明确设置 success = true
    }

    public static <T> ApiControllerResponse<T> success(String message) {
        return new ApiControllerResponse<>(200, message, true, null); // 明确设置 success = true
    }

    // 失败响应 (推荐使用这个，明确设置 success = false)
    public static <T> ApiControllerResponse<T> error(int code, String message) {
        return new ApiControllerResponse<>(code, message, false, null); // 明确设置 success = false
    }

    // 默认的失败响应
    public static <T> ApiControllerResponse<T> error(String message) {
        return new ApiControllerResponse<>(500, message, false, null); // 默认500, 明确设置 success = false
    }

    // 如果你的全参构造函数是 @AllArgsConstructor 自动生成的，它会包含所有字段
    // 如果你手动写了构造函数，请确保它包含 Boolean success 字段
}
