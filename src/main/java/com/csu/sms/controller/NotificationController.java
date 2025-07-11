package com.csu.sms.controller;

import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.model.user.UserNotification;

import com.csu.sms.service.NotificationService;
import com.csu.sms.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    // 获取用户通知列表
    @GetMapping
    public ApiControllerResponse<PageResult<UserNotification>> getUserNotifications(
            // ✨ 修改：移除了 @RequestHeader("Authorization") String token
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        PageResult<UserNotification> notifications = notificationService.getUserNotifications(currentUserId, page, size);
        return ApiControllerResponse.success(notifications);
    }

    // 获取未读通知数量
    @GetMapping("/unread/count")
    public ApiControllerResponse<Integer> getUnreadCount(
            // ✨ 修改：移除了 @RequestHeader("Authorization") String token
    ) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        int count = notificationService.getUnreadCount(currentUserId);
        return ApiControllerResponse.success(count);
    }

    // 将单条通知标记为已读
    @PutMapping("/{id}/read")
    public ApiControllerResponse<Boolean> markAsRead(
            @PathVariable Long id
            // ✨ 修改：移除了 @RequestHeader("Authorization") String token
    ) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = notificationService.markAsRead(id, currentUserId);
        return ApiControllerResponse.success(success);
    }

    // 将所有通知标记为已读
    @PutMapping("/read/all")
    public ApiControllerResponse<Boolean> markAllAsRead(
            // ✨ 修改：移除了 @RequestHeader("Authorization") String token
    ) {
        // ✨ 修改：从 UserContext 获取用户ID，并且强制要求用户必须登录
        Long currentUserId = UserContext.getRequiredCurrentUserId();
        boolean success = notificationService.markAllAsRead(currentUserId);
        return ApiControllerResponse.success(success);
    }
}









//package com.csu.sms.controller;
//
//import com.csu.sms.common.ApiControllerResponse;
//import com.csu.sms.common.PageResult;
//import com.csu.sms.model.user.UserNotification;
//import com.csu.sms.service.NotificationService;
//import com.csu.sms.util.JwtUtil;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//@CrossOrigin(origins = "http://localhost:5173")
//@RestController
//@RequestMapping("/api/notifications")
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationController {
//    @Autowired
//    private NotificationService notificationService;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @GetMapping
//    public ApiControllerResponse<PageResult<UserNotification>> getUserNotifications(
//            @RequestHeader("Authorization") String token,
//            @RequestParam(defaultValue = "1") Integer page,
//            @RequestParam(defaultValue = "10") Integer size
//    ) {
//        Long userId=jwtUtil.extractUserId(token);
//        PageResult<UserNotification> notifications = notificationService.getUserNotifications(userId, page, size);
//        return ApiControllerResponse.success(notifications);
//    }
//
//    @GetMapping("/unread/count")
//    public ApiControllerResponse<Integer> getUnreadCount(@RequestHeader("Authorization") String token) {
//        Long userId=jwtUtil.extractUserId(token);
//        int count = notificationService.getUnreadCount(userId);
//        return ApiControllerResponse.success(count);
//    }
//
//    @PutMapping("/{id}/read")
//    public ApiControllerResponse<Boolean> markAsRead(
//            @PathVariable Long id,
//            @RequestHeader("Authorization") String token
//    ) {
//        Long userId=jwtUtil.extractUserId(token);
//        boolean success = notificationService.markAsRead(id, userId);
//        return ApiControllerResponse.success(success);
//    }
//
//    @PutMapping("/read/all")
//    public ApiControllerResponse<Boolean> markAllAsRead(@RequestHeader("Authorization") String token) {
//        Long userId=jwtUtil.extractUserId(token);
//        boolean success = notificationService.markAllAsRead(userId);
//        return ApiControllerResponse.success(success);
//    }
//}
