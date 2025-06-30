package com.csu.sms.controller;

import com.csu.sms.common.ApiControllerResponse;
import com.csu.sms.common.PageResult;
import com.csu.sms.model.user.UserNotification;
import com.csu.sms.service.NotificationService;
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

    @GetMapping
    public ApiControllerResponse<PageResult<UserNotification>> getUserNotifications(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResult<UserNotification> notifications = notificationService.getUserNotifications(userId, page, size);
        return ApiControllerResponse.success(notifications);
    }

    @GetMapping("/unread/count")
    public ApiControllerResponse<Integer> getUnreadCount(@RequestParam Long userId) {
        int count = notificationService.getUnreadCount(userId);
        return ApiControllerResponse.success(count);
    }

    @PutMapping("/{id}/read")
    public ApiControllerResponse<Boolean> markAsRead(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        boolean success = notificationService.markAsRead(id, userId);
        return ApiControllerResponse.success(success);
    }

    @PutMapping("/read/all")
    public ApiControllerResponse<Boolean> markAllAsRead(@RequestParam Long userId) {
        boolean success = notificationService.markAllAsRead(userId);
        return ApiControllerResponse.success(success);
    }
}
