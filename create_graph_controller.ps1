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
 * 关系图控制器
 * 对应Python的Graph API路由
 */
@RestController
@RequestMapping("/api/novels/{novelId}/graph")
@RequiredArgsConstructor
@Slf4j
public class GraphController {

    private final ChapterFactRepository chapterFactRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取人物关系图数据
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGraph(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        
        Map<String, Map<String, Object>> nodeMap = new HashMap<>();
        Map<String, Set<String>> relationMap = new HashMap<>();
        
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        
        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            
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
                            
                            if (!nodeMap.containsKey(name)) {
                                Map<String, Object> node = new HashMap<>();
                                node.put("id", name);
                                node.put("name", name);
                                node.put("type", "person");
                                node.put("weight", 0);
                                nodeMap.put(name, node);
                            }
                            
                            Map<String, Object> node = nodeMap.get(name);
                            node.put("weight", (Integer) node.get("weight") + 1);
                            
                            if (characterNode.has("relations")) {
                                JsonNode relationsNode = characterNode.get("relations");
                                if (relationsNode.isArray()) {
                                    for (JsonNode relationNode : relationsNode) {
                                        if (relationNode.has("target") && relationNode.has("type")) {
                                            String target = relationNode.get("target").asText();
                                            String relationType = relationNode.get("type").asText();
                                            
                                            String edgeKey = name + "->" + target;
                                            String reverseKey = target + "->" + name;
                                            
                                            if (!relationMap.containsKey(edgeKey) && !relationMap.containsKey(reverseKey)) {
                                                Map<String, Object> edge = new HashMap<>();
                                                edge.put("source", name);
                                                edge.put("target", target);
                                                edge.put("type", relationType);
                                                edges.add(edge);
                                                
                                                relationMap.put(edgeKey, new HashSet<>());
                                                relationMap.get(edgeKey).add(relationType);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }
        
        nodes.addAll(nodeMap.values());
        
        nodes.sort((a, b) -> {
            Integer weightA = (Integer) a.get("weight");
            Integer weightB = (Integer) b.get("weight");
            return weightB.compareTo(weightA);
        });
        
        if (nodes.size() > 100) {
            nodes = nodes.subList(0, 100);
        }
        
        Set<String> nodeIds = new HashSet<>();
        for (Map<String, Object> node : nodes) {
            nodeIds.add((String) node.get("id"));
        }
        
        List<Map<String, Object>> filteredEdges = new ArrayList<>();
        for (Map<String, Object> edge : edges) {
            String source = (String) edge.get("source");
            String target = (String) edge.get("target");
            if (nodeIds.contains(source) && nodeIds.contains(target)) {
                filteredEdges.add(edge);
            }
        }
        
        result.put("nodes", nodes);
        result.put("edges", filteredEdges);
        result.put("node_count", nodes.size());
        result.put("edge_count", filteredEdges.size());
        
        return ResponseEntity.ok(result);
    }
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\controller\GraphController.java" -Value $content -Encoding UTF8