package com.csu.sms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI配置类
 */
@Component
@ConfigurationProperties(prefix = "alibaba.dashscope")
public class AIConfig {
    
    private String apiKey;
    private String model = "qwen-turbo";
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
} 