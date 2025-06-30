package com.csu.sms.scheduler;

import com.csu.sms.service.ExamBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExamBookingScheduledTasks {

    @Autowired
    private ExamBookingService examBookingService;

    /**
     * 每天凌晨1点处理过期预约
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void handleExpiredBookings() {
        try {
            examBookingService.handleExpiredBookings();
            System.out.println("过期预约处理完成：" + java.time.LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("处理过期预约失败：" + e.getMessage());
        }
    }

    /**
     * 每天上午9点发送考试提醒（提前1天）
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyExamReminders() {
        try {
            // 这里可以查询明天有考试的所有考试ID，然后发送提醒
            // examBookingService.sendExamReminder(examId);
            System.out.println("考试提醒发送完成：" + java.time.LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("发送考试提醒失败：" + e.getMessage());
        }
    }

    /**
     * 每小时清理过期通知
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredNotifications() {
        try {
            // 清理30天前的已读通知
            // examBookingService.cleanExpiredNotifications(30);
            System.out.println("过期通知清理完成：" + java.time.LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("清理过期通知失败：" + e.getMessage());
        }
    }
}