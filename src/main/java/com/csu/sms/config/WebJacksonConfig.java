package com.csu.sms.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary; // <-- 引入这个注解

@Configuration
public class WebJacksonConfig { // 命名更明确，表示这是为Web层配置的Jackson

    /**
     * 配置用于HTTP消息转换的ObjectMapper。
     * 确保此ObjectMapper不激活默认类型，因为你的DTO不需要。
     * 使用 @Primary 确保它成为处理 HTTP 请求的首选 ObjectMapper。
     */
    @Bean
    @Primary // *** 告诉Spring，这个是默认的ObjectMapper，优先使用它 ***
    public ObjectMapper httpMessageObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 禁用日期转时间戳，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());

        // *** 核心：这里绝对不要调用 objectMapper.activateDefaultTyping(...) ***
        // 因为你的 ForumPostDTO 是简单的POJO，不需要多态信息

        return objectMapper;
    }
}
