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
                log.warn("解析章节事实失败: chapterId={}, error={}", chapter.getId(), e.getMessage());
            }
        }
        
        if (type != null && !type.isEmpty()) {
            entities.removeIf(e -> !type.equals(e.get("type")));
        }
        
        result.put("entities", entities);
        result.put("total", entities.size());
        
        return ResponseEntity.ok(result);
    }
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\controller\EntityController.java" -Value $content -Encoding UTF8