package com.csu.sms.controller;

import com.csu.sms.dto.FaceRegisterRequest;
import com.csu.sms.dto.FaceLoginRequest;
import com.csu.sms.service.FaceAuthService;
import com.csu.sms.common.ApiResponse;
import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.ServiceException;
import com.csu.sms.annotation.LogOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceAuthController {
    @Autowired
    private FaceAuthService faceAuthService;

    @LogOperation(module = "用户认证", operation = "注册", description = "人脸注册")
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody FaceRegisterRequest request) {
        String result = faceAuthService.register(request);
        if ("注册成功".equals(result)) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error(result);
        }
    }

    @LogOperation(module = "用户认证", operation = "人脸登录", description = "用户通过人脸识别登录系统")
    @PostMapping("/login")
    public ApiControllerResponse<Map<String, Object>> login(@RequestBody FaceLoginRequest request) {
        try {
            Map<String, Object> result = faceAuthService.login(request);
            return ApiControllerResponse.success(result);
        } catch (ServiceException e) {
            return ApiControllerResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ApiControllerResponse.error(500, "服务器内部错误，人脸登录失败，请稍后再试。");
        }
    }
}