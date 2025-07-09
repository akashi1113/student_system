package com.csu.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (使用更简洁的写法)
                .csrf(AbstractHttpConfigurer::disable)
                
                // CORS 配置 (保留跨域设置)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("*"));  // 允许的前端
                    config.setAllowedOrigins(List.of("http://localhost:5173","http://localhost:5174"));  // 允许的前端源
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // 允许的HTTP方法
                    config.setAllowedHeaders(List.of("*"));  // 允许所有请求头
                    config.setAllowCredentials(true); // 允许携带cookie
                    return config;
                }))
                
                // 授权配置 (保留权限设置)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()  // 允许所有请求无需认证
                        .anyRequest().authenticated()  // 其他请求需要认证
                )
                
                // 会话管理 (保留无状态设置)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 无状态会话
                );
        return http.build();

    }
}
