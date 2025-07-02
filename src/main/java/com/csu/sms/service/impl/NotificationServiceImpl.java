package com.csu.sms.service.impl;

import com.csu.sms.common.PageResult;
import com.csu.sms.model.SystemLog;
import com.csu.sms.model.user.UserNotification;
import com.csu.sms.persistence.NotificationDao;
import com.csu.sms.service.LogService;
import com.csu.sms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationDao notificationDao;
    @Autowired
    private LogService logService;

    @Override
    public void sendNotification(Long userId, String title, String content, Integer type, Long relatedId) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setStatus(0); // 未读
        notification.setRelatedId(relatedId);
        notification.setCreateTime(LocalDateTime.now());

        try {
            notificationDao.insertNotification(notification);
            log.info("Notification sent to user {}: {}", userId, title);
            // 记录系统日志（模拟邮件/通知发送成功）
            recordNotifySystemLog("INFO", "通知服务", "通知发送成功", "通知已发送给用户: " + userId + ", 标题: " + title, null);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
            // 记录系统日志（模拟邮件/通知发送失败）
            recordNotifySystemLog("ERROR", "通知服务", "通知发送失败", e.getMessage(), null);
        }
    }

    @Override
    public PageResult<UserNotification> getUserNotifications(Long userId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<UserNotification> notifications = notificationDao.findByUserId(userId, offset, size);
        int total = notificationDao.countUnreadByUserId(userId);

        return PageResult.of(notifications, total, page, size);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationDao.countUnreadByUserId(userId);
    }

    @Override
    public boolean markAsRead(Long notificationId, Long userId) {
        return notificationDao.markAsRead(notificationId) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        return notificationDao.markAllAsRead(userId) > 0;
    }

    private void recordNotifySystemLog(String level, String type, String title, String content, String extra) {
        try {
            SystemLog log = new SystemLog();
            log.setLevel(level);
            log.setType(type);
            log.setTitle(title);
            log.setContent(content + (extra != null ? "\n" + extra : ""));
            log.setSource("NotificationServiceImpl");
            logService.recordSystemLog(log);
        } catch (Exception ignore) {}
    }
}
