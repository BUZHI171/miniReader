package com.aireader.v2.controller;

import com.aireader.v2.model.entity.AnalysisTask;
import com.aireader.v2.repository.AnalysisTaskRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析任务控制器
 * 对应Python的Analysis API接口
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisTaskRepository analysisTaskRepository;
    private final ChapterRepository chapterRepository;

    /**
     * 获取所有已启动分析任务列表
     */
    @GetMapping("/api/analysis/active")
    public ResponseEntity<Map<String, Object>> getAllActiveTasks() {
        List<AnalysisTask> runningTasks = analysisTaskRepository.findByStatus("running");
        List<AnalysisTask> pausedTasks = analysisTaskRepository.findByStatus("paused");

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
     * 获取分析任务状态
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
     * 获取活跃分析任务列表
     */
    @GetMapping("/api/novels/{novelId}/analysis/active")
    public ResponseEntity<Map<String, Object>> getActiveTasks(@PathVariable String novelId) {
        List<AnalysisTask> activeTasks = analysisTaskRepository.findByStatus("running");
        return ResponseEntity.ok(Map.of("tasks", activeTasks));
    }

    /**
     * 开始分析任务
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
        
        try {
            String taskId = analysisService.start(novelId, 1, (int) totalChapters, false);
            var taskOpt = analysisTaskRepository.findById(taskId);
            
            result.put("ok", true);
            result.put("task", taskOpt.orElse(null));
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            result.put("ok", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 暂停分析任务
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
        
        try {
            analysisService.pause(task.getId());
            result.put("ok", true);
            result.put("task", analysisTaskRepository.findById(task.getId()).orElse(task));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 恢复分析任务
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
        
        try {
            analysisService.resume(task.getId());
            result.put("ok", true);
            result.put("task", analysisTaskRepository.findById(task.getId()).orElse(task));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 取消分析任务
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
        
        try {
            analysisService.cancel(taskOpt.get().getId());
            result.put("ok", true);
            result.put("task", analysisTaskRepository.findById(taskOpt.get().getId()).orElse(taskOpt.get()));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("ok", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取最新任务信息
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

    /**
     * 获取实时计时信息
     */
    @GetMapping("/api/novels/{novelId}/analysis/timing")
    public ResponseEntity<Map<String, Object>> getAnalysisTiming(@PathVariable String novelId) {
        Map<String, Object> timing = analysisService.getLiveTiming(novelId);
        return ResponseEntity.ok(Map.of("timing", timing));
    }

    /**
     * 获取重试进度
     */
    @GetMapping("/api/novels/{novelId}/analysis/retry-progress")
    public ResponseEntity<Map<String, Object>> getRetryProgress(@PathVariable String novelId) {
        Map<String, Object> progress = analysisService.getRetryProgress(novelId);
        return ResponseEntity.ok(Map.of("retry_progress", progress));
    }

    /**
     * 重试失败章节
     */
    @PostMapping("/api/novels/{novelId}/analysis/retry-failed")
    public ResponseEntity<Map<String, Object>> retryFailedChapters(@PathVariable String novelId) {
        Map<String, Integer> result = analysisService.retryFailedChapters(novelId);
        return ResponseEntity.ok(Map.of("ok", true, "retried", result.get("retried"), "total", result.get("total")));
    }

    /**
     * 获取正在重试的小说列表
     */
    @GetMapping("/api/analysis/retrying")
    public ResponseEntity<Map<String, Object>> getRetryingNovels() {
        List<String> novelIds = analysisService.getRetryingNovelIds();
        return ResponseEntity.ok(Map.of("novel_ids", novelIds));
    }
}