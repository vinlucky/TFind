package com.tfind.toilet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class EnvCheckConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvCheckConfig.class);

    @Value("${ai.dashscope.api-key:}")
    private String dashscopeApiKey;

    @PostConstruct
    public void checkEnvConfiguration() {
        if (dashscopeApiKey == null || dashscopeApiKey.isEmpty()) {
            logger.warn("DASHSCOPE_API_KEY is not configured! Please set it in .env file");
        } else {
            logger.info("DASHSCOPE_API_KEY is configured successfully");
        }
    }
}
