package com.aireader.v2.controller;

import com.aireader.v2.model.entity.UsageEvent;
import com.aireader.v2.repository.UsageEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 使用分析 - 本地匿名事件跟踪
 */
@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@Slf4j
public class UsageController {

    private final UsageEventRepository usageEventRepository;
    private final ObjectMapper objectMapper;

    @Data
    public static class TrackEventRequest {
        private String event_type;
        private Map<String, Object> metadata;
    }

    /**
     * 记录使用事件（匿名，仅本地）
     */
    @PostMapping("/track")
    public ResponseEntity<Map<String, Object>> trackEvent(@RequestBody TrackEventRequest body) {
        if (!isTrackingEnabled()) {
            return ResponseEntity.ok(Map.of("ok", false, "reason", "tracking_disabled"));
        }

        try {
            String metadataJson = body.getMetadata() != null 
                ? objectMapper.writeValueAsString(body.getMetadata()) 
                : "{}";

            UsageEvent event = UsageEvent.builder()
                    .id(usageEventRepository.getMaxId() + 1)
                    .eventType(body.getEvent_type())
                    .metadata(metadataJson)
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

            usageEventRepository.save(event);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to track event: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    /**
     * GET 版本的 track（用于简单事件）
     */
    @GetMapping("/track")
    public ResponseEntity<Map<String, Object>> trackEventGet(
            @RequestParam(required = false) String event_type,
            @RequestParam(required = false) String metadata) {
        
        if (!isTrackingEnabled()) {
            return ResponseEntity.ok(Map.of("ok", false, "reason", "tracking_disabled"));
        }

        if (event_type == null || event_type.isEmpty()) {
            return ResponseEntity.ok(Map.of("ok", true));  // 静默返回成功
        }

        try {
            UsageEvent event = UsageEvent.builder()
                    .id(usageEventRepository.getMaxId() + 1)
                    .eventType(event_type)
                    .metadata(metadata != null ? metadata : "{}")
                    .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

            usageEventRepository.save(event);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("Failed to track event: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("ok", true));  // 静默返回成功
        }
    }

    /**
     * 获取事件统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam(defaultValue = "30") int days) {

        days = Math.max(1, Math.min(365, days));
        String daysParam = "-" + days + " days";

        // 获取事件类型统计
        List<Object[]> statsRows = usageEventRepository.getEventStats(daysParam);
        List<Map<String, Object>> byType = new ArrayList<>();
        for (Object[] row : statsRows) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("event_type", row[0]);
            stat.put("count", row[1]);
            byType.add(stat);
        }

        // 获取每日趋势
        List<Object[]> trendRows = usageEventRepository.getDailyTrend(daysParam);
        List<Map<String, Object>> dailyTrend = new ArrayList<>();
        for (Object[] row : trendRows) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("day", row[0]);
            trend.put("count", row[1]);
            dailyTrend.add(trend);
        }

        long total = usageEventRepository.getTotalCount();

        Map<String, Object> response = new HashMap<>();
        response.put("total_events", total);
        response.put("by_type", byType);
        response.put("daily_trend", dailyTrend);
        response.put("days", days);

        return ResponseEntity.ok(response);
    }

    /**
     * 清除所有事件
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearEvents() {
        int deleted = usageEventRepository.clearAllEvents();
        return ResponseEntity.ok(Map.of("ok", true, "deleted", deleted));
    }

    /**
     * 获取跟踪状态
     */
    @GetMapping("/tracking-enabled")
    public ResponseEntity<Map<String, Object>> getTrackingStatus() {
        boolean enabled = isTrackingEnabled();
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }

    /**
     * 设置跟踪状态
     */
    @PutMapping("/tracking-enabled")
    public ResponseEntity<Map<String, Object>> setTrackingStatus(@RequestBody Map<String, Object> body) {
        boolean enabled = body.containsKey("enabled") && Boolean.TRUE.equals(body.get("enabled"));
        // TODO: 持久化到 app_settings 表
        return ResponseEntity.ok(Map.of("ok", true, "enabled", enabled));
    }

    /**
     * 检查是否启用跟踪（默认启用）
     */
    private boolean isTrackingEnabled() {
        // TODO: 从 app_settings 读取
        return true;
    }
}
