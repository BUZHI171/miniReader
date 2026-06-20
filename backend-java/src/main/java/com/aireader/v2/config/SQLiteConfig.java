package com.aireader.v2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite 数据库配置 - 启用 WAL 模式以解决并发锁定问题
 */
@Configuration
@Slf4j
public class SQLiteConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @PostConstruct
    public void enableWALMode() {
        // 提取数据库路径
        String dbPath = datasourceUrl.replace("jdbc:sqlite:", "");
        
        try (Connection conn = DriverManager.getConnection(datasourceUrl);
             Statement stmt = conn.createStatement()) {
            
            // 启用 WAL 模式
            stmt.execute("PRAGMA journal_mode=WAL");
            log.info("SQLite WAL mode enabled for: {}", dbPath);
            
            // 设置 busy_timeout 为 30 秒
            stmt.execute("PRAGMA busy_timeout=30000");
            
            // 设置 synchronous 为 NORMAL 以提高性能
            stmt.execute("PRAGMA synchronous=NORMAL");
            
        } catch (SQLException e) {
            log.warn("Failed to set SQLite PRAGMA settings: {}", e.getMessage());
        }
    }
}
