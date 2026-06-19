$content = @'
package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 百科控制器
 * 对应Python的Encyclopedia API路由
 */
@RestController
@RequestMapping("/api/novels/{novelId}/encyclopedia")
@RequiredArgsConstructor
@Slf4j
public class EncyclopediaController {

    private final ChapterFactRepository chapterFactRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取百科分类统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats(@PathVariable String novelId) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("person", 0);
        stats.put("location", 0);
        stats.put("item", 0);
        stats.put("org", 0);
        stats.put("concept", 0);
        
        Set<String> countedEntities = new HashSet<>();
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        
        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            
            if (factOpt.isEmpty()) {
                continue;
            }
            
            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());
                
                countEntities(factJson, "characters", "person", stats, countedEntities);
                countEntities(factJson, "locations", "location", stats, countedEntities);
                countEntities(factJson, "items", "item", stats, countedEntities);
                countEntities(factJson, "organizations", "org", stats, countedEntities);
                countEntities(factJson, "concepts", "concept", stats, countedEntities);
                
            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }
        
        int total = stats.values().stream().mapToInt(Integer::intValue).sum();
        
        Map<String, Object> result = new HashMap<>();
        result.put("categories", stats);
        result.put("total", total);
        
        return ResponseEntity.ok(result);
    }
    
    private void countEntities(JsonNode factJson, String nodeName, String type, 
                               Map<String, Integer> stats, Set<String> countedEntities) {
        JsonNode node = factJson.get(nodeName);
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.has("name")) {
                    String name = item.get("name").asText();
                    String key = type + ":" + name;
                    if (!countedEntities.contains(key)) {
                        stats.merge(type, 1, Integer::sum);
                        countedEntities.add(key);
                    }
                }
            }
        }
    }

    /**
     * 获取百科条目列表
     */
    @GetMapping("/entries")
    public ResponseEntity<Map<String, Object>> listEntries(
            @PathVariable String novelId,
            @RequestParam(required = false) String category) {
        
        List<Map<String, Object>> entries = new ArrayList<>();
        Map<String, Map<String, Object>> entityMap = new HashMap<>();
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        
        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            
            if (factOpt.isEmpty()) {
                continue;
            }
            
            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());
                
                processEntries(factJson, "characters", "person", entityMap, chapter, category);
                processEntries(factJson, "locations", "location", entityMap, chapter, category);
                processEntries(factJson, "items", "item", entityMap, chapter, category);
                processEntries(factJson, "organizations", "org", entityMap, chapter, category);
                processEntries(factJson, "concepts", "concept", entityMap, chapter, category);
                
            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }
        
        entries.addAll(entityMap.values());
        entries.sort((a, b) -> {
            Integer countA = (Integer) a.getOrDefault("appearance_count", 0);
            Integer countB = (Integer) b.getOrDefault("appearance_count", 0);
            return countB.compareTo(countA);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("entries", entries);
        result.put("total", entries.size());
        
        return ResponseEntity.ok(result);
    }
    
    private void processEntries(JsonNode factJson, String nodeName, String type,
                                Map<String, Map<String, Object>> entityMap, 
                                Chapter chapter, String categoryFilter) {
        if (categoryFilter != null && !categoryFilter.isEmpty() && !categoryFilter.equals(type)) {
            return;
        }
        
        JsonNode node = factJson.get(nodeName);
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.has("name")) {
                    String name = item.get("name").asText();
                    String key = type + ":" + name;
                    
                    if (!entityMap.containsKey(key)) {
                        Map<String, Object> entity = new HashMap<>();
                        entity.put("name", name);
                        entity.put("type", type);
                        entity.put("appearance_count", 0);
                        entity.put("first_chapter", chapter.getChapterNum());
                        entityMap.put(key, entity);
                    }
                    
                    Map<String, Object> entity = entityMap.get(key);
                    entity.put("appearance_count", (Integer) entity.get("appearance_count") + 1);
                    entity.put("last_chapter", chapter.getChapterNum());
                    
                    if (item.has("description") && !entity.containsKey("description")) {
                        entity.put("description", item.get("description").asText());
                    }
                }
            }
        }
    }

    /**
     * 获取概念详情
     */
    @GetMapping("/concepts/{conceptName}")
    public ResponseEntity<Map<String, Object>> getConceptDetail(
            @PathVariable String novelId,
            @PathVariable String conceptName) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> appearances = new ArrayList<>();
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        
        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            
            if (factOpt.isEmpty()) {
                continue;
            }
            
            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());
                
                JsonNode conceptsNode = factJson.get("concepts");
                if (conceptsNode != null && conceptsNode.isArray()) {
                    for (JsonNode conceptNode : conceptsNode) {
                        if (conceptNode.has("name") && conceptName.equals(conceptNode.get("name").asText())) {
                            Map<String, Object> appearance = new HashMap<>();
                            appearance.put("chapter_num", chapter.getChapterNum());
                            appearance.put("chapter_title", chapter.getTitle());
                            
                            if (conceptNode.has("description")) {
                                appearance.put("description", conceptNode.get("description").asText());
                            }
                            if (conceptNode.has("category")) {
                                appearance.put("category", conceptNode.get("category").asText());
                            }
                            
                            appearances.add(appearance);
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }
        
        if (appearances.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        result.put("name", conceptName);
        result.put("type", "concept");
        result.put("appearances", appearances);
        result.put("appearance_count", appearances.size());
        
        return ResponseEntity.ok(result);
    }
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\controller\EncyclopediaController.java" -Value $content -Encoding UTF8