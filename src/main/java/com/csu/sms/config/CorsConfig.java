package com.csu.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS跨域配置
 * @author CSU Team
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置CORS跨域策略
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源，这里配置前端地址
                .allowedOriginPatterns("*")
                // 或者明确指定前端地址
                // .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许发送Cookie
                .allowCredentials(true)
                // 预检请求的有效期，单位为秒
                .maxAge(3600);
    }

    /**
     * 配置CORS配置源（用于更精细的控制）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许所有源（开发环境）
        configuration.addAllowedOriginPattern("*");
        // 生产环境建议明确指定源
        // configuration.addAllowedOrigin("http://localhost:8080");
        // configuration.addAllowedOrigin("http://127.0.0.1:8080");
        
        // 允许所有方法
        configuration.addAllowedMethod("*");
        
        // 允许所有请求头
        configuration.addAllowedHeader("*");
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 预检请求的有效期
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 