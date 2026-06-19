$content = @'
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
 * 分析任务控制器
 * 对应Python的Analysis API路由
 */
@RestController
@RequestMapping("/api/novels/{novelId}/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisTaskRepository analysisTaskRepository;
    private final ChapterRepository chapterRepository;

    /**
     * 获取分析任务状态
     */
    @GetMapping
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
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveTasks() {
        List<AnalysisTask> activeTasks = analysisTaskRepository.findByStatus("running");
        return ResponseEntity.ok(Map.of("tasks", activeTasks));
    }

    /**
     * 开始分析任务
     */
    @PostMapping("/start")
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
     * 暂停分析任务
     */
    @PostMapping("/pause")
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
     * 恢复分析任务
     */
    @PostMapping("/resume")
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
     * 取消分析任务
     */
    @PostMapping("/cancel")
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
     * 获取最新任务信息
     */
    @GetMapping("/latest")
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
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\controller\AnalysisController.java" -Value $content -Encoding UTF8