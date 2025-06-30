package com.csu.sms.service;

import com.csu.sms.common.PageResult;
import com.csu.sms.model.user.UserNotification;

public interface NotificationService {
    public void sendNotification(Long userId, String title, String content, Integer type, Long relatedId);
    public PageResult<UserNotification> getUserNotifications(Long userId, Integer page, Integer size);
    public int getUnreadCount(Long userId);
    public boolean markAsRead(Long notificationId, Long userId);
    public boolean markAllAsRead(Long userId);
}
