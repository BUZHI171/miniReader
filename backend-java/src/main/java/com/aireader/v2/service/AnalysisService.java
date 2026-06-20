package com.aireader.v2.service;

import com.aireader.v2.model.entity.AnalysisTask;
import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.model.entity.WorldStructure;
import com.aireader.v2.repository.AnalysisTaskRepository;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.NovelRepository;
import com.aireader.v2.repository.WorldStructureRepository;
import com.aireader.v2.util.IdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Orchestrate full-novel chapter analysis.
 */
@Slf4j
@Service
public class AnalysisService {

    private final AnalysisTaskRepository analysisTaskRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterFactRepository chapterFactRepository;
    private final NovelRepository novelRepository;
    private final WorldStructureRepository worldStructureRepository;
    private final ObjectMapper objectMapper;
    private final ConnectionManager connectionManager;
    private final FactExtractor factExtractor;
    private final ContextBuilder contextBuilder;

    // Track running tasks for pause/cancel
    private final Map<String, String> taskSignals = new ConcurrentHashMap<>();  // task_id -> desired status
    private final Set<String> activeLoops = ConcurrentHashMap.newKeySet();       // task_ids with currently-running loops
    // Live timing stats per novel (survives page navigation)
    private final Map<String, Map<String, Object>> liveTiming = new ConcurrentHashMap<>();
    // Retry progress per novel (survives page navigation)
    private final Map<String, Map<String, Object>> retryProgress = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public AnalysisService(AnalysisTaskRepository analysisTaskRepository,
                          ChapterRepository chapterRepository,
                          ChapterFactRepository chapterFactRepository,
                          NovelRepository novelRepository,
                          WorldStructureRepository worldStructureRepository,
                          ObjectMapper objectMapper,
                          ConnectionManager connectionManager,
                          FactExtractor factExtractor,
                          ContextBuilder contextBuilder) {
        this.analysisTaskRepository = analysisTaskRepository;
        this.chapterRepository = chapterRepository;
        this.chapterFactRepository = chapterFactRepository;
        this.novelRepository = novelRepository;
        this.worldStructureRepository = worldStructureRepository;
        this.objectMapper = objectMapper;
        this.connectionManager = connectionManager;
        this.factExtractor = factExtractor;
        this.contextBuilder = contextBuilder;
    }

    @PostConstruct
    public void init() {
        log.info("AnalysisService initialized");
    }

    /**
     * Return live timing stats for a running analysis, or null.
     */
    public Map<String, Object> getLiveTiming(String novelId) {
        return liveTiming.get(novelId);
    }

    /**
     * Return retry progress for a novel, or null.
     */
    public Map<String, Object> getRetryProgress(String novelId) {
        return retryProgress.get(novelId);
    }

    /**
     * Return novel IDs with active retries.
     */
    public List<String> getRetryingNovelIds() {
        return new ArrayList<>(retryProgress.keySet());
    }

    /**
     * Start analysis, returns task_id. The analysis loop runs as a background task.
     *
     * @param force If true, re-analyze even already-completed chapters.
     *              If false (default), skip chapters with analysis_status='completed'.
     */
    @Transactional
    public String start(String novelId, int chapterStart, int chapterEnd, boolean force) {
        // Check if there's already a running task
        Optional<AnalysisTask> existing = analysisTaskRepository.findRunningTaskByNovelId(novelId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Novel " + novelId + " already has an active task: " + existing.get().getId());
        }

        // Ensure pre-scan is done before analysis (skip on force re-analyze)
        if (!force) {
            ensurePrescan(novelId);
        }

        String taskId = String.valueOf(IdGenerator.generateId());
        AnalysisTask task = AnalysisTask.builder()
                .id(taskId)
                .novelId(novelId)
                .chapterStart(chapterStart)
                .chapterEnd(chapterEnd)
                .status("running")
                .currentChapter(chapterStart)
                .createdAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
        analysisTaskRepository.save(task);

        taskSignals.put(taskId, "running");

        // Launch background analysis loop
        executorService.submit(() -> runLoop(taskId, novelId, chapterStart, chapterEnd, force));

        return taskId;
    }

    /**
     * Resume a paused task.
     */
    @Transactional
    public void resume(String taskId) {
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task " + taskId + " not found");
        }
        AnalysisTask task = taskOpt.get();
        if (!"paused".equals(task.getStatus())) {
            throw new IllegalArgumentException("Task " + taskId + " is not paused (status=" + task.getStatus() + ")");
        }

        analysisTaskRepository.updateStatus(taskId, "running");
        taskSignals.put(taskId, "running");

        String novelId = task.getNovelId();
        broadcast(novelId, Map.of(
                "type", "task_status",
                "status", "running"
        ));

        // Only start a new loop if the old one has fully exited
        if (!activeLoops.contains(taskId)) {
            int resumeFrom = task.getCurrentChapter() + 1;
            int chapterEnd = task.getChapterEnd();
            executorService.submit(() -> runLoop(taskId, novelId, resumeFrom, chapterEnd, false));
        }
    }

    /**
     * Signal a running task to pause after current chapter.
     */
    @Transactional
    public void pause(String taskId) {
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task " + taskId + " not found");
        }
        AnalysisTask task = taskOpt.get();
        taskSignals.put(taskId, "paused");

        // Immediate DB + broadcast so frontend updates without waiting for the loop
        analysisTaskRepository.updateStatus(taskId, "paused");
        broadcast(task.getNovelId(), Map.of(
                "type", "task_status",
                "status", "paused"
        ));
    }

    /**
     * Signal a running task to cancel after current chapter.
     */
    @Transactional
    public void cancel(String taskId) {
        Optional<AnalysisTask> taskOpt = analysisTaskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task " + taskId + " not found");
        }
        AnalysisTask task = taskOpt.get();
        taskSignals.put(taskId, "cancelled");

        // Immediate DB + broadcast so frontend updates without waiting for the loop
        analysisTaskRepository.updateStatus(taskId, "cancelled");
        broadcast(task.getNovelId(), Map.of(
                "type", "task_status",
                "status", "cancelled"
        ));
    }

    /**
     * Ensure pre-scan is done before analysis starts.
     */
    private void ensurePrescan(String novelId) {
        // TODO: Implement prescan check
        log.debug("Prescan check skipped for novel: {}", novelId);
    }

    /**
     * Main analysis loop. Runs as a background task.
     */
    private void runLoop(String taskId, String novelId, int chapterStart, int chapterEnd, boolean force) {
        activeLoops.add(taskId);
        try {
            runLoopInner(taskId, novelId, chapterStart, chapterEnd, force);
        } finally {
            activeLoops.remove(taskId);
        }
    }

    /**
     * Inner analysis loop body.
     */
    private void runLoopInner(String taskId, String novelId, int chapterStart, int chapterEnd, boolean force) {
        int total = chapterEnd - chapterStart + 1;
        Map<String, Integer> stats = new HashMap<>();
        stats.put("entities", 0);
        stats.put("relations", 0);
        stats.put("events", 0);

        // Timing tracking
        List<Long> chapterTimes = new ArrayList<>();
        long analysisStartMs = System.currentTimeMillis();
        // Track chapters that failed during this run (for auto-retry)
        List<Map<String, Object>> failedInRun = new ArrayList<>();
        // Quality tracking
        Map<String, Integer> qualityStats = new HashMap<>();
        qualityStats.put("truncated_count", 0);
        qualityStats.put("segmented_count", 0);
        qualityStats.put("total_segments", 0);

        // Pre-compute stats from existing chapter facts
        if (!force) {
            List<ChapterFact> existingFacts = chapterFactRepository.findByNovelId(novelId);
            for (ChapterFact ef : existingFacts) {
                long chId = ef.getChapterId();
                if (chId >= chapterStart && chId <= chapterEnd) {
                    try {
                        Map<String, Object> factData = objectMapper.readValue(ef.getFactJson(), Map.class);
                        List<?> characters = (List<?>) factData.getOrDefault("characters", Collections.emptyList());
                        List<?> locations = (List<?>) factData.getOrDefault("locations", Collections.emptyList());
                        List<?> relationships = (List<?>) factData.getOrDefault("relationships", Collections.emptyList());
                        List<?> events = (List<?>) factData.getOrDefault("events", Collections.emptyList());

                        stats.merge("entities", characters.size() + locations.size(), Integer::sum);
                        stats.merge("relations", relationships.size(), Integer::sum);
                        stats.merge("events", events.size(), Integer::sum);
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to parse fact JSON for chapter {}", chId, e);
                    }
                }
            }
        }

        // Broadcast initial state
        broadcast(novelId, Map.of(
                "type", "progress",
                "chapter", chapterStart,
                "total", total,
                "done", 0,
                "stats", stats
        ));

        for (int chapterNum = chapterStart; chapterNum <= chapterEnd; chapterNum++) {
            // Check for pause/cancel signal
            String signal = taskSignals.getOrDefault(taskId, "running");
            if ("paused".equals(signal)) {
                log.info("Task {} loop stopping (paused) at chapter {}", taskId, chapterNum);
                return;
            }
            if ("cancelled".equals(signal)) {
                log.info("Task {} loop stopping (cancelled) at chapter {}", taskId, chapterNum);
                taskSignals.remove(taskId);
                liveTiming.remove(novelId);
                return;
            }

            // Get chapter content
            Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chapterNum);
            if (chapterOpt.isEmpty()) {
                log.warn("Chapter {} not found for novel {}, skipping", chapterNum, novelId);
                int doneCount = chapterNum - chapterStart + 1;
                broadcastProgress(novelId, chapterNum, total, doneCount, stats, null, chapterTimes, analysisStartMs);
                continue;
            }

            Chapter chapter = chapterOpt.get();

            // Skip excluded chapters
            if (Boolean.TRUE.equals(chapter.getIsExcluded())) {
                log.debug("Skipping excluded chapter {}", chapterNum);
                analysisTaskRepository.updateProgress(taskId, chapterNum);
                int doneCount = chapterNum - chapterStart + 1;
                broadcastProgress(novelId, chapterNum, total, doneCount, stats, null, chapterTimes, analysisStartMs);
                continue;
            }

            // Skip already-completed chapters unless force=true
            if (!force && "completed".equals(chapter.getAnalysisStatus())) {
                log.debug("Skipping already-completed chapter {}", chapterNum);
                analysisTaskRepository.updateProgress(taskId, chapterNum);
                int doneCount = chapterNum - chapterStart + 1;
                broadcastProgress(novelId, chapterNum, total, doneCount, stats, null, chapterTimes, analysisStartMs);
                continue;
            }

            // Broadcast "processing" before LLM call
            broadcast(novelId, Map.of(
                    "type", "processing",
                    "chapter", chapterNum,
                    "total", total,
                    "timing", liveTiming.getOrDefault(novelId, Collections.emptyMap())
            ));

            long startMs = System.currentTimeMillis();

            try {
                // Build context summary
                broadcastStage(novelId, chapterNum, "构建上下文");
                String context = buildContext(novelId, chapterNum);

                // Extract facts
                broadcastStage(novelId, chapterNum, "AI 提取中");
                Map<String, Object> fact = extractFacts(novelId, chapterNum, chapter.getContent(), context);

                // Validate
                broadcastStage(novelId, chapterNum, "验证数据");
                fact = validateFact(fact);

                // Resolve name variants
                fact = resolveNames(fact);

                // Update world structure
                broadcastStage(novelId, chapterNum, "更新世界结构");
                boolean worldStructureUpdated = updateWorldStructure(novelId, chapterNum, chapter.getContent(), fact);

                // Save data
                broadcastStage(novelId, chapterNum, "保存数据");
                long elapsedMs = System.currentTimeMillis() - startMs;
                chapterTimes.add(elapsedMs);
                updateLiveTiming(novelId, chapterTimes, analysisStartMs, total, chapterNum - chapterStart + 1);

                // Store fact
                storeChapterFact(novelId, chapter.getId(), fact, elapsedMs);

                // Scene extraction
                broadcastStage(novelId, chapterNum, "场景分析");
                extractScenes(novelId, chapter.getId(), chapter.getContent(), fact);

                // Index embeddings
                indexEmbeddings(novelId, chapterNum, chapter.getContent(), fact);

                // Update chapter status
                chapterRepository.updateAnalysisStatus(novelId, chapterNum, "completed");

                // Update cumulative stats
                List<?> characters = (List<?>) fact.getOrDefault("characters", Collections.emptyList());
                List<?> locations = (List<?>) fact.getOrDefault("locations", Collections.emptyList());
                List<?> relationships = (List<?>) fact.getOrDefault("relationships", Collections.emptyList());
                List<?> events = (List<?>) fact.getOrDefault("events", Collections.emptyList());

                stats.merge("entities", characters.size() + locations.size(), Integer::sum);
                stats.merge("relations", relationships.size(), Integer::sum);
                stats.merge("events", events.size(), Integer::sum);

                // Broadcast chapter done
                broadcast(novelId, Map.of(
                        "type", "chapter_done",
                        "chapter", chapterNum,
                        "status", "completed",
                        "world_structure_updated", worldStructureUpdated
                ));

            } catch (Exception e) {
                long elapsedMs = System.currentTimeMillis() - startMs;
                chapterTimes.add(elapsedMs);
                updateLiveTiming(novelId, chapterTimes, analysisStartMs, total, chapterNum - chapterStart + 1);

                String errType = classifyError(e);
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 500) {
                    errMsg = errMsg.substring(0, 500);
                }

                log.error("Error processing chapter {} [{}]: {}", chapterNum, errType, e.getMessage());

                chapterRepository.updateAnalysisStatus(novelId, chapterNum, "failed", errMsg, errType);

                Map<String, Object> failedChapter = new HashMap<>();
                failedChapter.put("chapter_number", chapterNum);
                failedChapter.put("error_type", errType);
                failedChapter.put("content", chapter.getContent());
                failedInRun.add(failedChapter);

                broadcast(novelId, Map.of(
                        "type", "chapter_done",
                        "chapter", chapterNum,
                        "status", "failed",
                        "error", errMsg,
                        "error_type", errType
                ));
            }

            // Update task progress
            analysisTaskRepository.updateProgress(taskId, chapterNum);

            // Broadcast overall progress
            int doneCount = chapterNum - chapterStart + 1;
            broadcastProgress(novelId, chapterNum, total, doneCount, stats, null, chapterTimes, analysisStartMs);
        }

        // Auto-retry failed chapters
        if (!failedInRun.isEmpty()) {
            log.info("Auto-retrying {} failed chapters", failedInRun.size());
            broadcastStage(novelId, chapterEnd, "重试 " + failedInRun.size() + " 个失败章节");

            for (Map<String, Object> retryCh : failedInRun) {
                int retryNum = (Integer) retryCh.get("chapter_number");
                String errorType = (String) retryCh.get("error_type");

                // Skip content_policy chapters (will always be rejected)
                if ("content_policy".equals(errorType)) {
                    log.info("Skipping retry for chapter {}: content_policy", retryNum);
                    continue;
                }

                try {
                    long retryStartMs = System.currentTimeMillis();
                    String content = (String) retryCh.get("content");
                    String context = buildContext(novelId, retryNum);
                    Map<String, Object> fact = extractFacts(novelId, retryNum, content, context);
                    fact = validateFact(fact);

                    long retryElapsed = System.currentTimeMillis() - retryStartMs;
                    storeChapterFact(novelId, (Long) retryCh.get("id"), fact, retryElapsed);
                    chapterRepository.updateAnalysisStatus(novelId, retryNum, "completed");

                    List<?> characters = (List<?>) fact.getOrDefault("characters", Collections.emptyList());
                    List<?> locations = (List<?>) fact.getOrDefault("locations", Collections.emptyList());
                    List<?> relationships = (List<?>) fact.getOrDefault("relationships", Collections.emptyList());
                    List<?> events = (List<?>) fact.getOrDefault("events", Collections.emptyList());

                    stats.merge("entities", characters.size() + locations.size(), Integer::sum);
                    stats.merge("relations", relationships.size(), Integer::sum);
                    stats.merge("events", events.size(), Integer::sum);

                    broadcast(novelId, Map.of(
                            "type", "chapter_done",
                            "chapter", retryNum,
                            "status", "retry_success"
                    ));
                    log.info("Auto-retry succeeded for chapter {}", retryNum);
                } catch (Exception e) {
                    String errType = classifyError(e);
                    String errMsg = e.getMessage();
                    if (errMsg != null && errMsg.length() > 500) {
                        errMsg = errMsg.substring(0, 500);
                    }
                    log.warn("Auto-retry failed for chapter {} [{}]: {}", retryNum, errType, e.getMessage());
                    chapterRepository.updateAnalysisStatus(novelId, retryNum, "failed", errMsg, errType);
                }
            }
        }

        // Persist timing summary
        if (!chapterTimes.isEmpty()) {
            Map<String, Object> timingSummary = new HashMap<>();
            timingSummary.put("total_ms", System.currentTimeMillis() - analysisStartMs);
            timingSummary.put("avg_chapter_ms", chapterTimes.stream().mapToLong(Long::longValue).average().orElse(0));
            timingSummary.put("min_chapter_ms", chapterTimes.stream().mapToLong(Long::longValue).min().orElse(0));
            timingSummary.put("max_chapter_ms", chapterTimes.stream().mapToLong(Long::longValue).max().orElse(0));
            timingSummary.put("chapters_processed", chapterTimes.size());

            try {
                String timingJson = objectMapper.writeValueAsString(timingSummary);
                analysisTaskRepository.saveTimingSummary(taskId, timingJson);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize timing summary: {}", e.getMessage());
            }
        }

        // Post-analysis: location hierarchy enhancement
        try {
            broadcastStage(novelId, chapterEnd, "优化地点层级");
            enhanceLocationHierarchy(novelId);
        } catch (Exception e) {
            log.warn("Post-analysis hierarchy enhancement failed: {}", e.getMessage());
        }

        // Auto-generate synopsis
        try {
            broadcastStage(novelId, chapterEnd, "生成小说概要");
            autoGenerateSynopsis(novelId);
        } catch (Exception e) {
            log.warn("Synopsis auto-generation failed: {}", e.getMessage());
        }

        // Final status
        broadcastStage(novelId, chapterEnd, "完成分析");
        List<Chapter> remainingFailures = chapterRepository.findFailedChapters(novelId);
        String finalStatus = remainingFailures.isEmpty() ? "completed" : "completed_with_errors";
        analysisTaskRepository.updateStatus(taskId, finalStatus);

        Map<String, Object> completedMsg = new HashMap<>();
        completedMsg.put("type", "task_status");
        completedMsg.put("status", finalStatus);
        completedMsg.put("stats", stats);
        broadcast(novelId, completedMsg);

        taskSignals.remove(taskId);
        liveTiming.remove(novelId);

        log.info("Task {} completed for novel {}", taskId, novelId);

        // Auto-trigger post-analysis pipeline
        if ("completed".equals(finalStatus) || "completed_with_errors".equals(finalStatus)) {
            executorService.submit(() -> autoRebuildHierarchy(novelId));
            executorService.submit(() -> autoSpatialCompletion(novelId));
        }
    }

    private void broadcastStage(String novelId, int chapter, String label) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "stage");
        data.put("chapter", chapter);
        data.put("stage_label", label);
        // TODO: Add llm_model and llm_provider
        broadcast(novelId, data);
    }

    private void broadcastProgress(String novelId, int chapter, int total, int done,
                                   Map<String, Integer> stats, Map<String, Object> costStats,
                                   List<Long> chapterTimes, long analysisStartMs) {
        Map<String, Object> progressMsg = new HashMap<>();
        progressMsg.put("type", "progress");
        progressMsg.put("chapter", chapter);
        progressMsg.put("total", total);
        progressMsg.put("done", done);
        progressMsg.put("stats", stats);

        if (costStats != null) {
            progressMsg.put("cost", costStats);
        }

        if (!chapterTimes.isEmpty()) {
            long avgMs = (long) chapterTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            int remaining = total - done;

            Map<String, Object> timing = new HashMap<>();
            timing.put("last_chapter_ms", chapterTimes.get(chapterTimes.size() - 1));
            timing.put("avg_chapter_ms", avgMs);
            timing.put("elapsed_total_ms", System.currentTimeMillis() - analysisStartMs);
            timing.put("eta_ms", avgMs * remaining);

            progressMsg.put("timing", timing);
        }

        broadcast(novelId, progressMsg);
    }

    private void updateLiveTiming(String novelId, List<Long> chapterTimes,
                                  long analysisStartMs, int total, int doneCount) {
        long avgMs = (long) chapterTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        int remaining = total - doneCount;

        Map<String, Object> timing = new HashMap<>();
        timing.put("last_chapter_ms", chapterTimes.get(chapterTimes.size() - 1));
        timing.put("avg_chapter_ms", avgMs);
        timing.put("elapsed_total_ms", System.currentTimeMillis() - analysisStartMs);
        timing.put("eta_ms", avgMs * remaining);

        liveTiming.put(novelId, timing);
    }

    private String classifyError(Exception e) {
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (msg.contains("timeout") || msg.contains("timed out")) {
            return "timeout";
        }
        if (msg.contains("content_filter") || msg.contains("content_policy") ||
            msg.contains("sensitive") || msg.contains("blocked") ||
            msg.contains("safety") || msg.contains("moderation") ||
            msg.contains("inappropriate") || msg.contains("涉黄") ||
            msg.contains("涉暴") || msg.contains("违规") || msg.contains("审核")) {
            return "content_policy";
        }
        if (msg.contains("http") || msg.contains("connection") || msg.contains("request")) {
            return "http_error";
        }
        if (msg.contains("parse") || msg.contains("json") || msg.contains("syntax")) {
            return "parse_error";
        }
        return "unknown";
    }

    // ==================== Implementation methods ====================

    private String buildContext(String novelId, int chapterNum) {
        return contextBuilder.buildContext(novelId, chapterNum);
    }

    private Map<String, Object> extractFacts(String novelId, int chapterId, String chapterText, String context) {
        return factExtractor.extract(novelId, chapterId, chapterText, context);
    }

    private Map<String, Object> validateFact(Map<String, Object> fact) {
        // TODO: Implement fact validation
        return fact;
    }

    private Map<String, Object> resolveNames(Map<String, Object> fact) {
        // TODO: Implement name resolution
        return fact;
    }

    private boolean updateWorldStructure(String novelId, int chapterNum, String content, Map<String, Object> fact) {
        // TODO: Implement world structure update
        return false;
    }

    private void storeChapterFact(String novelId, long chapterId, Map<String, Object> fact, long elapsedMs) {
        try {
            String factJson = objectMapper.writeValueAsString(fact);
            ChapterFact chapterFact = ChapterFact.builder()
                    .novelId(novelId)
                    .chapterId(chapterId)
                    .factJson(factJson)
                    .extractionMs((int) elapsedMs)
                    .build();
            chapterFactRepository.save(chapterFact);
        } catch (JsonProcessingException e) {
            log.error("Failed to store chapter fact: {}", e.getMessage());
        }
    }

    private void extractScenes(String novelId, long chapterId, String content, Map<String, Object> fact) {
        // TODO: Implement scene extraction
    }

    private void indexEmbeddings(String novelId, int chapterNum, String content, Map<String, Object> fact) {
        // TODO: Implement embedding indexing
    }

    private void enhanceLocationHierarchy(String novelId) {
        // TODO: Implement location hierarchy enhancement
    }

    private void autoGenerateSynopsis(String novelId) {
        // TODO: Implement synopsis generation
    }

    private void autoRebuildHierarchy(String novelId) {
        // TODO: Implement auto hierarchy rebuild
    }

    private void autoSpatialCompletion(String novelId) {
        // TODO: Implement auto spatial completion
    }

    /**
     * Start retrying failed chapters in the background. Returns immediately.
     */
    public Map<String, Integer> retryFailedChapters(String novelId) {
        List<Chapter> failedChapters = chapterRepository.findFailedChapters(novelId);

        if (failedChapters.isEmpty()) {
            return Map.of("retried", 0, "total", 0);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Chapter ch : failedChapters) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", ch.getId());
            row.put("chapter_num", ch.getChapterNum());
            row.put("content", ch.getContent());
            rows.add(row);
        }

        executorService.submit(() -> retryFailedBg(novelId, rows));
        return Map.of("retried", rows.size(), "total", rows.size());
    }

    private void retryFailedBg(String novelId, List<Map<String, Object>> rows) {
        int total = rows.size();
        int succeeded = 0;
        int failedCount = 0;

        retryProgress.put(novelId, Map.of("total", total, "done", 0, "current_chapter", 0));

        broadcast(novelId, Map.of(
                "type", "retry_start",
                "total", total
        ));

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            long chId = (Long) row.get("id");
            int chNum = (Integer) row.get("chapter_num");
            String chContent = (String) row.get("content");

            retryProgress.put(novelId, Map.of("total", total, "done", i, "current_chapter", chNum));
            broadcast(novelId, Map.of(
                    "type", "retry_progress",
                    "chapter", chNum,
                    "done", i,
                    "total", total
            ));

            try {
                String context = buildContext(novelId, chNum);
                Map<String, Object> fact = extractFacts(novelId, chNum, chContent, context);
                fact = validateFact(fact);

                storeChapterFact(novelId, chId, fact, 0);
                chapterRepository.updateAnalysisStatus(novelId, chNum, "completed");

                broadcast(novelId, Map.of(
                        "type", "chapter_done",
                        "chapter", chNum,
                        "status", "retry_success"
                ));
                succeeded++;
            } catch (Exception e) {
                String errType = classifyError(e);
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 500) {
                    errMsg = errMsg.substring(0, 500);
                }
                log.warn("Manual retry failed for chapter {} [{}]: {}", chNum, errType, e.getMessage());
                chapterRepository.updateAnalysisStatus(novelId, chNum, "failed", errMsg, errType);

                broadcast(novelId, Map.of(
                        "type", "chapter_done",
                        "chapter", chNum,
                        "status", "failed",
                        "error", errMsg,
                        "error_type", errType
                ));
                failedCount++;
            }
        }

        // Update task status if all failures resolved
        Optional<AnalysisTask> latestTask = analysisTaskRepository.findLatestTask(novelId);
        if (latestTask.isPresent() && "completed_with_errors".equals(latestTask.get().getStatus())) {
            List<Chapter> stillFailed = chapterRepository.findFailedChapters(novelId);
            if (stillFailed.isEmpty()) {
                analysisTaskRepository.updateStatus(latestTask.get().getId(), "completed");
            }
        }

        retryProgress.remove(novelId);

        broadcast(novelId, Map.of(
                "type", "retry_done",
                "total", total,
                "succeeded", succeeded,
                "failed", failedCount
        ));
    }

    private void broadcast(String novelId, Map<String, Object> data) {
        connectionManager.broadcast(novelId, data);
    }
}