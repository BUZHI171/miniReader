package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.NovelRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/novels/{novelId}/scenes")
@RequiredArgsConstructor
@Slf4j
public class SceneController {

    private final ChapterFactRepository chapterFactRepository;
    private final ChapterRepository chapterRepository;
    private final NovelRepository novelRepository;
    private final ObjectMapper objectMapper;

    @Data
    public static class Scene {
        private String location;
        private String time;
        private String characters;
        private String action;
        private Integer start;
        private Integer end;
    }

    // GET /api/novels/{novelId}/scenes/{chapterNum}
    @GetMapping("/{chapterNum}")
    public ResponseEntity<Map<String, Object>> getChapterScenes(
            @PathVariable String novelId,
            @PathVariable Integer chapterNum) {

        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        // 获取章节内容
        Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chapterNum);
        if (chapterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Chapter chapter = chapterOpt.get();

        // 尝试从chapter_facts获取LLM提取的场景
        Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
        
        if (factOpt.isPresent() && factOpt.get().getScenesJson() != null) {
            try {
                List<Scene> scenes = objectMapper.readValue(
                        factOpt.get().getScenesJson(),
                        new TypeReference<List<Scene>>() {}
                );
                
                Map<String, Object> response = new HashMap<>();
                response.put("chapter", chapterNum);
                response.put("scenes", scenes);
                response.put("scene_count", scenes.size());
                response.put("source", "llm");
                return ResponseEntity.ok(response);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse scenes_json: {}", e.getMessage());
            }
        }

        // 回退到基于规则的场景提取
        List<Scene> scenes = extractScenesFromContent(chapter.getContent());
        
        Map<String, Object> response = new HashMap<>();
        response.put("chapter", chapterNum);
        response.put("scenes", scenes);
        response.put("scene_count", scenes.size());
        response.put("source", "rule");
        return ResponseEntity.ok(response);
    }

    // GET /api/novels/{novelId}/scenes
    @GetMapping
    public ResponseEntity<Map<String, Object>> getScenesRange(
            @PathVariable String novelId,
            @RequestParam Integer chapterStart,
            @RequestParam Integer chapterEnd) {

        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        Map<Integer, List<Scene>> chaptersScenes = new TreeMap<>();
        int totalScenes = 0;

        for (int chNum = chapterStart; chNum <= chapterEnd; chNum++) {
            Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chNum);
            if (chapterOpt.isEmpty()) continue;

            Chapter chapter = chapterOpt.get();
            List<Scene> scenes;

            // 尝试从chapter_facts获取
            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            if (factOpt.isPresent() && factOpt.get().getScenesJson() != null) {
                try {
                    scenes = objectMapper.readValue(
                            factOpt.get().getScenesJson(),
                            new TypeReference<List<Scene>>() {}
                    );
                } catch (JsonProcessingException e) {
                    scenes = extractScenesFromContent(chapter.getContent());
                }
            } else {
                scenes = extractScenesFromContent(chapter.getContent());
            }

            chaptersScenes.put(chNum, scenes);
            totalScenes += scenes.size();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("chapter_start", chapterStart);
        response.put("chapter_end", chapterEnd);
        response.put("chapters", chaptersScenes);
        response.put("total_scenes", totalScenes);
        return ResponseEntity.ok(response);
    }

    /**
     * 基于规则的场景提取（简化版）
     */
    private List<Scene> extractScenesFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptyList();
        }

        List<Scene> scenes = new ArrayList<>();
        
        // 简单规则：按段落分割，每段作为一个场景
        String[] paragraphs = content.split("\n\n+");
        
        for (int i = 0; i < Math.min(paragraphs.length, 10); i++) {
            String para = paragraphs[i].trim();
            if (para.length() < 10) continue;
            
            Scene scene = new Scene();
            scene.setAction(truncate(para, 200));
            scene.setStart(i);
            scene.setEnd(i + 1);
            scenes.add(scene);
        }

        return scenes;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
