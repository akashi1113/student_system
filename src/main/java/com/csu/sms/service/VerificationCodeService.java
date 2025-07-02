package com.csu.sms.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class VerificationCodeService {

    // 使用内存缓存验证码（生产环境建议用Redis）
    private final Map<String, String> emailCodeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> codeCreationTimeMap = new ConcurrentHashMap<>();
    private static final long CODE_EXPIRATION = 5 * 60 * 1000; // 5分钟有效

    public String generateCode(String email) {
        log.info("Generating verification code for email: {}", email);
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        emailCodeMap.put(email, code);
        codeCreationTimeMap.put(email, System.currentTimeMillis());
        log.info("Generated code: {} for email: {}", code, email);
        return code;
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = emailCodeMap.get(email);
        Long creationTime = codeCreationTimeMap.get(email);
        if (storedCode == null || creationTime == null) {
            return false;
        }

        // 检查验证码是否过期
        if (System.currentTimeMillis() - creationTime > CODE_EXPIRATION) {
            removeCode(email);
            return false;
        }

        return storedCode.equals(code);
    }

    public void removeCode(String email) {
        emailCodeMap.remove(email);
        codeCreationTimeMap.remove(email);
    }
}
