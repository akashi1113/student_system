package com.csu.sms.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.core.JsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebJacksonConfig {

    /**
     * 支持多格式 LocalDateTime 反序列化器
     */
    public static class MultiFormatLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter[] FORMATTERS = new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        };
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText().trim();
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    return LocalDateTime.parse(value, formatter);
                } catch (Exception ignored) {}
            }
            throw new IOException("无法解析日期时间: " + value);
        }
    }

    /**
     * 配置用于HTTP消息转换的ObjectMapper。
     * 支持多种日期时间格式
     */
    @Bean
    @Primary
    public ObjectMapper httpMessageObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 禁用日期转时间戳，使用ISO-8601格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 创建Java 8日期时间模块
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 序列化用全局格式，反序列化支持多格式
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new MultiFormatLocalDateTimeDeserializer());
        
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }
}
