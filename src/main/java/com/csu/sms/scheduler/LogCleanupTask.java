package com.csu.sms.scheduler;

import com.csu.sms.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 日志清理定时任务
 */
@Component
@Slf4j
public class LogCleanupTask {
    
    @Autowired
    private LogService logService;
    
    /**
     * 每天凌晨2点清理30天前的日志
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredLogs() {
        log.info("开始执行日志清理任务");
        try {
            logService.cleanupExpiredLogs(30);
            log.info("日志清理任务执行完成");
        } catch (Exception e) {
            log.error("日志清理任务执行失败", e);
        }
    }
}
 