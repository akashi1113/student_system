package com.csu.sms;

import com.csu.sms.model.SystemLog;
import com.csu.sms.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class StudentSystemApplication {

	@Autowired
	private LogService logService;

	public static void main(String[] args) {
		SpringApplication.run(StudentSystemApplication.class, args);
	}

	/**
	 * 应用启动完成后记录系统日志
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		try {
			SystemLog systemLog = new SystemLog();
			systemLog.setLevel("INFO");
			systemLog.setType("系统启动");
			systemLog.setTitle("学生管理系统启动成功");
			systemLog.setContent("系统启动成功，所有服务正常运行，端口：8080");
			systemLog.setSource("StudentSystemApplication");
			
			logService.recordSystemLog(systemLog);
			log.info("系统启动日志已记录");
		} catch (Exception e) {
			log.error("记录系统启动日志失败", e);
		}
	}
}
