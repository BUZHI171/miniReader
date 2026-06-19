package com.aireader.v2.util;

import java.util.Random;

/**
 * ID生成器工具类
 * 使用时间戳+随机数生成Long类型ID，避免SQLite IDENTITY问题
 */
public class IdGenerator {

    private static final Random RANDOM = new Random();
    private static long lastTimestamp = 0;

    /**
     * 生成Long类型ID
     * 格式：时间戳(毫秒) + 随机数(3位)
     */
    public static synchronized long generateId() {
        long timestamp = System.currentTimeMillis();
        
        // 如果时间戳相同，等待直到不同
        if (timestamp <= lastTimestamp) {
            timestamp = lastTimestamp + 1;
        }
        lastTimestamp = timestamp;
        
        // 添加随机数确保唯一性
        int random = RANDOM.nextInt(1000);
        
        // 组合时间戳和随机数
        return timestamp * 1000 + random;
    }

    /**
     * 生成Long类型ID（静态方法别名）
     */
    public static synchronized Long nextId() {
        return generateId();
    }
}