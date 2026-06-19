package com.aireader.v2.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LLM配置属性类
 */
@Configuration
@ConfigurationProperties(prefix = "aireader.llm")
@Data
public class LlmConfig {
    
    private String provider = "ollama";
    private String model = "qwen3:8b";
}
