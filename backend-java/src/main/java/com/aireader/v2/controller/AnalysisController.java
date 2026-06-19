package com.aireader.v2.controller;

import com.aireader.v2.model.entity.AnalysisTask;
import com.aireader.v2.repository.AnalysisTaskRepository;
import com.aireader.v2.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 鍒嗘瀽浠诲姟鎺у埗鍣?
 * 瀵瑰簲Python鐨凙nalysis API璺敱
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisTaskRepository analysisTaskRepository;
    private final ChapterRepository chapterRepository;

    /**
     * 鑾峰彇鎵�鏈夊凡鍚�姹俛pi/analysis/active
     */
    @GetMapping("/api/analysis/active")
    public ResponseEntity<Map<String, Object>> getAllActiveTasks() {
        List<AnalysisTask> runningTasks = analysisTaskRepository.findByStatus("running");
        List<AnalysisTask> pausedTasks = analysisTaskRepository.findByStatus("paused");

        // 褰卞叆running浠诲姟浠�
        Map<String, String> result = new HashMap<>();
        for (AnalysisTask task : runningTasks) {
            result.put(task.getNovelId(), "running");
        }
        for (AnalysisTask task : pausedTasks) {
            if (!result.containsKey(task.getNovelId())) {
                result.put(task.getNovelId(), "paused");
            }
        }

        List<Map<String, String>> items = result.entrySet().stream()
                .map(e -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("novel_id", e.getKey());
                    item.put("status", e.getValue());
                    return item;
                })
                .toList();

        return ResponseEntity.ok(Map.of("items", items));
    }

    /**
     * 鑾峰彇鍒嗘瀽浠诲姟鐘舵€?
     */
    @GetMapping("/api/novels/{novelId}/analysis")
    public ResponseEntity<Map<String, Object>> getAnalysisStatus(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        var taskOpt = analysisTaskRepository.findByNovelId(novelId);
        
        if (taskOpt.isEmpty()) {
            result.put("task", null);
            result.put("status", "not_started");
            return ResponseEntity.ok(result);
        }
        
        AnalysisTask task = taskOpt.get();
        result.put("task", task);
        result.put("status", task.getStatus());
        
        long totalChapters = chapterRepository.countByNovelId(novelId);
        long completedChapters = chapterRepository.countCompletedByNovelId(novelId);
        
        result.put("progress", totalChapters > 0 ? (double) completedChapters / totalChapters : 0);
        result.put("completed_chapters", completedChapters);
        result.put("total_chapters", totalChapters);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 鑾峰彇娲昏穬鍒嗘瀽浠诲姟鍒楄〃
     */
    @GetMapping("/api/novels/{novelId}/analysis/active")
    public ResponseEntity<Map<String, Object>> getActiveTasks(@PathVariable String novelId) {
        List<AnalysisTask> activeTasks = analysisTaskRepository.findByStatus("running");
        return ResponseEntity.ok(Map.of("tasks", activeTasks));
    }

    /**
     * 寮€濮嬪垎鏋愪换鍔?
     */
    @PostMapping("/api/novels/{novelId}/analysis/start")
    public ResponseEntity<Map<String, Object>> startAnalysis(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        long totalChapters = chapterRepository.countByNovelId(novelId);
        if (totalChapters == 0) {
            result.put("ok", false);
            result.put("error", "No chapters found for novel");
            return ResponseEntity.badRequest().body(result);
        }
        
        var existingTask = analysisTaskRepository.findByNovelId(novelId);
        if (existingTask.isPresent()) {
            AnalysisTask task = existingTask.get();
            if ("running".equals(task.getStatus())) {
                result.put("ok", false);
                result.put("error", "Analysis already running");
                return ResponseEntity.badRequest().body(result);
            }
            task.setStatus("running");
            task.setCurrentChapter(1);
            task.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            analysisTaskRepository.save(task);
            result.put("ok", true);
            result.put("task", task);
            return ResponseEntity.ok(result);
        }
        
        AnalysisTask newTask = AnalysisTask.builder()
                .id(UUID.randomUUID().toString())
                .novelId(novelId)
                .status("running")
                .chapterStart(1)
                .chapterEnd((int) totalChapters)
                .currentChapter(1)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        
        analysisTaskRepository.save(newTask);
        
        result.put("ok", true);
        result.put("task", newTask);
        return ResponseEntity.ok(result);
    }

    /**
     * 鏆傚仠鍒嗘瀽浠诲姟
     */
    @PostMapping("/api/novels/{novelId}/analysis/pause")
    public ResponseEntity<Map<String, Object>> pauseAnalysis(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        var taskOpt = analysisTaskRepository.findByNovelId(novelId);
        if (taskOpt.isEmpty()) {
            result.put("ok", false);
            result.put("error", "No active analysis task found");
            return ResponseEntity.badRequest().body(result);
        }
        
        AnalysisTask task = taskOpt.get();
        if (!"running".equals(task.getStatus())) {
            result.put("ok", false);
            result.put("error", "Task is not running");
            return ResponseEntity.badRequest().body(result);
        }
        
        task.setStatus("paused");
        task.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        analysisTaskRepository.save(task);
        
        result.put("ok", true);
        result.put("task", task);
        return ResponseEntity.ok(result);
    }

    /**
     * 鎭㈠畲鍒嗘瀽浠诲姟
     */
    @PostMapping("/api/novels/{novelId}/analysis/resume")
    public ResponseEntity<Map<String, Object>> resumeAnalysis(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        var taskOpt = analysisTaskRepository.findByNovelId(novelId);
        if (taskOpt.isEmpty()) {
            result.put("ok", false);
            result.put("error", "No analysis task found");
            return ResponseEntity.badRequest().body(result);
        }
        
        AnalysisTask task = taskOpt.get();
        if (!"paused".equals(task.getStatus())) {
            result.put("ok", false);
            result.put("error", "Task is not paused");
            return ResponseEntity.badRequest().body(result);
        }
        
        task.setStatus("running");
        task.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        analysisTaskRepository.save(task);
        
        result.put("ok", true);
        result.put("task", task);
        return ResponseEntity.ok(result);
    }

    /**
     * 鍙栨秷鍒嗘瀽浠诲姟
     */
    @PostMapping("/api/novels/{novelId}/analysis/cancel")
    public ResponseEntity<Map<String, Object>> cancelAnalysis(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        var taskOpt = analysisTaskRepository.findByNovelId(novelId);
        if (taskOpt.isEmpty()) {
            result.put("ok", false);
            result.put("error", "No analysis task found");
            return ResponseEntity.badRequest().body(result);
        }
        
        AnalysisTask task = taskOpt.get();
        task.setStatus("cancelled");
        task.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        analysisTaskRepository.save(task);
        
        result.put("ok", true);
        result.put("task", task);
        return ResponseEntity.ok(result);
    }

    /**
     * 鑾峰彇鏈�鏂颁换鍔′俊鎭?
     */
    @GetMapping("/api/novels/{novelId}/analysis/latest")
    public ResponseEntity<Map<String, Object>> getLatestTask(@PathVariable String novelId) {
        var taskOpt = analysisTaskRepository.findByNovelId(novelId);
        
        if (taskOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("task", null));
        }
        
        AnalysisTask task = taskOpt.get();
        long completedChapters = chapterRepository.countCompletedByNovelId(novelId);
        long failedChapters = chapterRepository.findFailedByNovelId(novelId).size();
        
        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("completed_chapters", completedChapters);
        result.put("failed_chapters", failedChapters);
        
        return ResponseEntity.ok(result);
    }
}
