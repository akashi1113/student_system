package com.csu.sms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CSU学生管理系统启动类
 * @author CSU Team
 */
@SpringBootApplication
@MapperScan("com.csu.sms.persistence")
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}