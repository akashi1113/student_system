package com.csu.sms.service;

import com.csu.sms.dto.FaceRegisterRequest;
import com.csu.sms.dto.FaceLoginRequest;
import com.csu.sms.model.user.User;
import com.csu.sms.persistence.UserDao;
import com.csu.sms.util.BaiduFaceApiUtil;
import com.csu.sms.util.UserContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class FaceAuthService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private BaiduFaceApiUtil baiduFaceApiUtil;
    @Autowired(required = false)
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 人脸注册
     */
    public String register(FaceRegisterRequest request) {
        // 1. 检查用户名是否已存在
        if (userDao.findByUsername(request.getUsername()) != null) {
            return "用户名已存在";
        }
        // 2. 保存用户基本信息
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder != null ? passwordEncoder.encode(request.getPassword()) : request.getPassword());
        user.setEmail(request.getEmail());
        userDao.insertUser(user);
        // 3. 注册人脸到百度云
        try {
            JsonNode result = baiduFaceApiUtil.faceRegister(String.valueOf(user.getId()), request.getFaceImage());
            if (result.get("error_code").asInt() == 0) {
                UserContext.setCurrentUserId(user.getId());
                UserContext.setCurrentUsername(user.getUsername());
                return "注册成功";
            } else {
                // 注册失败，回滚用户
                userDao.deleteUser(user.getId());
                return "人脸注册失败:" + result.get("error_msg").asText();
            }
        } catch (Exception e) {
            userDao.deleteUser(user.getId());
            return "人脸注册异常:" + e.getMessage();
        }
    }

    /**
     * 人脸登录
     */
    public String login(FaceLoginRequest request) {
        User user = userDao.findByUsername(request.getUsername());
        if (user == null) {
            return "用户不存在";
        }
        try {
            JsonNode result = baiduFaceApiUtil.faceSearch(request.getFaceImage());
            if (result.get("error_code").asInt() == 0) {
                JsonNode userList = result.get("result").get("user_list");
                if (userList != null && userList.size() > 0) {
                    JsonNode candidate = userList.get(0);
                    String candidateId = candidate.get("user_id").asText();
                    double score = candidate.get("score").asDouble();
                    if (score > 70) {
                        UserContext.setCurrentUserId(user.getId());
                        UserContext.setCurrentUsername(user.getUsername());
                        UserContext.setLoginType("FACE_LOGIN");
                        return "登录成功";
                    } else {
                        return "人脸匹配度不足，得分:" + score + "，需要70分以上";
                    }
                } else {
                    return "未检测到匹配人脸";
                }
            } else {
                return "人脸识别失败:" + result.get("error_msg").asText();
            }
        } catch (Exception e) {
            return "人脸识别异常:" + e.getMessage();
        }
    }
} 