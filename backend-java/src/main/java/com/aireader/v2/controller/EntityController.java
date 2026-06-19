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

@RestController
@RequestMapping("/api/novels/{novelId}/entities")
@RequiredArgsConstructor
@Slf4j
public class EntityController {

    private final ChapterFactRepository chapterFactRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listEntities(
            @PathVariable String novelId,
            @RequestParam(required = false) String type) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();
        
        Set<String> addedEntityNames = new HashSet<>();
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        
        for (Chapter chapter : chapters) {
            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            
            if (factOpt.isEmpty()) {
                continue;
            }
            
            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());
                
                JsonNode charactersNode = factJson.get("characters");
                if (charactersNode != null && charactersNode.isArray()) {
                    for (JsonNode characterNode : charactersNode) {
                        if (characterNode.has("name")) {
                            String name = characterNode.get("name").asText();
                            if (!addedEntityNames.contains(name)) {
                                Map<String, Object> entity = new HashMap<>();
                                entity.put("name", name);
                                entity.put("type", "person");
                                entities.add(entity);
                                addedEntityNames.add(name);
                            }
                        }
                    }
                }
                
                JsonNode locationsNode = factJson.get("locations");
                if (locationsNode != null && locationsNode.isArray()) {
                    for (JsonNode locationNode : locationsNode) {
                        if (locationNode.has("name")) {
                            String name = locationNode.get("name").asText();
                            if (!addedEntityNames.contains(name)) {
                                Map<String, Object> entity = new HashMap<>();
                                entity.put("name", name);
                                entity.put("type", "location");
                                entities.add(entity);
                                addedEntityNames.add(name);
                            }
                        }
                    }
                }
                
                JsonNode itemsNode = factJson.get("items");
                if (itemsNode != null && itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        if (itemNode.has("name")) {
                            String name = itemNode.get("name").asText();
                            if (!addedEntityNames.contains(name)) {
                                Map<String, Object> entity = new HashMap<>();
                                entity.put("name", name);
                                entity.put("type", "item");
                                entities.add(entity);
                                addedEntityNames.add(name);
                            }
                        }
                    }
                }
                
                JsonNode organizationsNode = factJson.get("organizations");
                if (organizationsNode != null && organizationsNode.isArray()) {
                    for (JsonNode orgNode : organizationsNode) {
                        if (orgNode.has("name")) {
                            String name = orgNode.get("name").asText();
                            if (!addedEntityNames.contains(name)) {
                                Map<String, Object> entity = new HashMap<>();
                                entity.put("name", name);
                                entity.put("type", "org");
                                entities.add(entity);
                                addedEntityNames.add(name);
                            }
                        }
                    }
                }
                
                JsonNode conceptsNode = factJson.get("concepts");
                if (conceptsNode != null && conceptsNode.isArray()) {
                    for (JsonNode conceptNode : conceptsNode) {
                        if (conceptNode.has("name")) {
                            String name = conceptNode.get("name").asText();
                            if (!addedEntityNames.contains(name)) {
                                Map<String, Object> entity = new HashMap<>();
                                entity.put("name", name);
                                entity.put("type", "concept");
                                entities.add(entity);
                                addedEntityNames.add(name);
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("瑙ｆ瀽绔犺妭浜嬪疄澶辫触: chapterId={}, error={}", chapter.getId(), e.getMessage());
            }
        }
        
        if (type != null && !type.isEmpty()) {
            entities.removeIf(e -> !type.equals(e.get("type")));
        }
        
        result.put("entities", entities);
        result.put("total", entities.size());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{entityName}")
    public ResponseEntity<Map<String, Object>> getEntityDetail(
            @PathVariable String novelId,
            @PathVariable String entityName) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> appearances = new ArrayList<>();
        String entityType = null;
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        
        for (Chapter chapter : chapters) {
            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            
            if (factOpt.isEmpty()) {
                continue;
            }
            
            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());
                
                String[] types = {"characters", "locations", "items", "organizations", "concepts"};
                String[] typeNames = {"person", "location", "item", "org", "concept"};
                
                for (int i = 0; i < types.length; i++) {
                    JsonNode node = factJson.get(types[i]);
                    if (node != null && node.isArray()) {
                        for (JsonNode itemNode : node) {
                            if (itemNode.has("name") && entityName.equals(itemNode.get("name").asText())) {
                                if (entityType == null) {
                                    entityType = typeNames[i];
                                }
                                
                                Map<String, Object> appearance = new HashMap<>();
                                appearance.put("chapter_num", chapter.getChapterNum());
                                appearance.put("chapter_title", chapter.getTitle());
                                
                                if (itemNode.has("description")) {
                                    appearance.put("description", itemNode.get("description").asText());
                                }
                                if (itemNode.has("role")) {
                                    appearance.put("role", itemNode.get("role").asText());
                                }
                                if (itemNode.has("status")) {
                                    appearance.put("status", itemNode.get("status").asText());
                                }
                                if (itemNode.has("relations")) {
                                    appearance.put("relations", itemNode.get("relations"));
                                }
                                
                                appearances.add(appearance);
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}, error={}", chapter.getId(), e.getMessage());
            }
        }
        
        if (entityType == null) {
            return ResponseEntity.notFound().build();
        }
        
        result.put("name", entityName);
        result.put("type", entityType);
        result.put("appearances", appearances);
        result.put("appearance_count", appearances.size());
        
        return ResponseEntity.ok(result);
    }
}
