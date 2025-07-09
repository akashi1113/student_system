package com.csu.sms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {
    @Value("${spark.api.password}")
    private String apiPassword;

    @Value("${spark.api.model:lite}")
    private String model;

    public String getApiPassword() {
        return apiPassword;
    }

    public String getModel() {
        return model;
    }
}