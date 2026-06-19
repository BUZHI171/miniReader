package com.aireader.v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI Reader V2 Backend Application
 * 小说分析工具后端主类
 */
@SpringBootApplication
@EnableAsync
public class AiReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiReaderApplication.class, args);
    }
}
