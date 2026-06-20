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
     * 获取百科分类统计 (根路径)
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getCategoryStats(@PathVariable String novelId) {
        return getCategoryStatsInternal(novelId);
    }

    /**
     * 获取百科分类统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStatsEndpoint(@PathVariable String novelId) {
        return getCategoryStatsInternal(novelId);
    }

    private ResponseEntity<Map<String, Object>> getCategoryStatsInternal(String novelId) {
        Set<String> persons = new HashSet<>();
        Set<String> locations = new HashSet<>();
        Set<String> items = new HashSet<>();
        Set<String> orgs = new HashSet<>();
        Map<String, Set<String>> conceptsByCategory = new HashMap<>();

        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);

        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());

            if (factOpt.isEmpty()) {
                continue;
            }

            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());

                // Characters
                JsonNode charactersNode = factJson.get("characters");
                if (charactersNode != null && charactersNode.isArray()) {
                    for (JsonNode ch : charactersNode) {
                        if (ch.has("name") && !ch.get("name").asText().isEmpty()) {
                            persons.add(ch.get("name").asText());
                        }
                    }
                }

                // Locations
                JsonNode locationsNode = factJson.get("locations");
                if (locationsNode != null && locationsNode.isArray()) {
                    for (JsonNode loc : locationsNode) {
                        if (loc.has("name") && !loc.get("name").asText().isEmpty()) {
                            locations.add(loc.get("name").asText());
                        }
                    }
                }

                // Item events
                JsonNode itemEventsNode = factJson.get("item_events");
                if (itemEventsNode != null && itemEventsNode.isArray()) {
                    for (JsonNode ie : itemEventsNode) {
                        if (ie.has("item_name") && !ie.get("item_name").asText().isEmpty()) {
                            items.add(ie.get("item_name").asText());
                        }
                    }
                }

                // Org events
                JsonNode orgEventsNode = factJson.get("org_events");
                if (orgEventsNode != null && orgEventsNode.isArray()) {
                    for (JsonNode oe : orgEventsNode) {
                        if (oe.has("org_name") && !oe.get("org_name").asText().isEmpty()) {
                            orgs.add(oe.get("org_name").asText());
                        }
                    }
                }

                // New concepts
                JsonNode conceptsNode = factJson.get("new_concepts");
                if (conceptsNode != null && conceptsNode.isArray()) {
                    for (JsonNode nc : conceptsNode) {
                        String name = nc.has("name") ? nc.get("name").asText() : "";
                        String cat = nc.has("category") ? nc.get("category").asText() : "其他";
                        if (!name.isEmpty()) {
                            conceptsByCategory.computeIfAbsent(cat, k -> new HashSet<>()).add(name);
                        }
                    }
                }

            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }

        // Calculate concept total
        int conceptTotal = conceptsByCategory.values().stream()
                .mapToInt(Set::size)
                .sum();

        // Build concept_categories map
        Map<String, Integer> conceptCategories = new TreeMap<>();
        for (Map.Entry<String, Set<String>> entry : conceptsByCategory.entrySet()) {
            conceptCategories.put(entry.getKey(), entry.getValue().size());
        }

        int total = persons.size() + locations.size() + items.size() + orgs.size() + conceptTotal;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("person", persons.size());
        result.put("location", locations.size());
        result.put("item", items.size());
        result.put("org", orgs.size());
        result.put("concept", conceptTotal);
        result.put("concept_categories", conceptCategories);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取百科条目列表
     */
    @GetMapping("/entries")
    public ResponseEntity<Map<String, Object>> listEntries(
            @PathVariable String novelId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "name") String sort) {

        Map<String, Map<String, Object>> entityMap = new LinkedHashMap<>();
        Map<String, Set<Integer>> entityChapters = new HashMap<>();

        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);

        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());

            if (factOpt.isEmpty()) {
                continue;
            }

            ChapterFact fact = factOpt.get();
            int chapterNum = chapter.getChapterNum();

            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());

                // Process characters (persons)
                if (category == null || category.isEmpty() || "person".equals(category)) {
                    processCharacters(factJson, entityMap, entityChapters, chapterNum);
                }

                // Process locations
                if (category == null || category.isEmpty() || "location".equals(category)) {
                    processLocations(factJson, entityMap, entityChapters, chapterNum);
                }

                // Process item events
                if (category == null || category.isEmpty() || "item".equals(category)) {
                    processItemEvents(factJson, entityMap, entityChapters, chapterNum);
                }

                // Process org events
                if (category == null || category.isEmpty() || "org".equals(category)) {
                    processOrgEvents(factJson, entityMap, entityChapters, chapterNum);
                }

                // Process new concepts
                if (category == null || category.isEmpty() || "concept".equals(category) ||
                        (category != null && !Arrays.asList("person", "location", "item", "org").contains(category))) {
                    processConcepts(factJson, entityMap, entityChapters, chapterNum, category);
                }

            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }

        // Add chapter_count to each entry
        for (Map.Entry<String, Map<String, Object>> entry : entityMap.entrySet()) {
            String name = entry.getKey();
            Set<Integer> chaptersSet = entityChapters.getOrDefault(name, Collections.emptySet());
            entry.getValue().put("chapter_count", chaptersSet.size());
        }

        List<Map<String, Object>> entries = new ArrayList<>(entityMap.values());

        // Sort entries
        if ("chapter".equals(sort)) {
            entries.sort(Comparator.comparingInt(e -> (Integer) e.getOrDefault("first_chapter", 0)));
        } else if ("mentions".equals(sort)) {
            entries.sort((a, b) -> {
                int countA = (Integer) a.getOrDefault("chapter_count", 0);
                int countB = (Integer) b.getOrDefault("chapter_count", 0);
                return countB - countA;
            });
        } else {
            entries.sort(Comparator.comparing(e -> (String) e.get("name")));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("entries", entries);

        return ResponseEntity.ok(result);
    }

    private void processCharacters(JsonNode factJson, Map<String, Map<String, Object>> entityMap,
                                    Map<String, Set<Integer>> entityChapters, int chapterNum) {
        JsonNode node = factJson.get("characters");
        if (node != null && node.isArray()) {
            for (JsonNode ch : node) {
                String name = ch.has("name") ? ch.get("name").asText() : "";
                if (name.isEmpty()) continue;

                entityChapters.computeIfAbsent(name, k -> new HashSet<>()).add(chapterNum);

                if (!entityMap.containsKey(name)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", name);
                    entry.put("type", "person");
                    entry.put("category", "person");
                    entry.put("definition", "");
                    entry.put("first_chapter", chapterNum);

                    // Build definition
                    StringBuilder def = new StringBuilder();
                    if (ch.has("appearance") && !ch.get("appearance").isNull()) {
                        String appearance = ch.get("appearance").asText();
                        def.append(appearance.length() > 50 ? appearance.substring(0, 50) : appearance);
                    }
                    if (ch.has("abilities_gained")) {
                        JsonNode abilities = ch.get("abilities_gained");
                        if (abilities.isArray() && abilities.size() > 0) {
                            for (int i = 0; i < Math.min(2, abilities.size()); i++) {
                                JsonNode ab = abilities.get(i);
                                if (def.length() > 0) def.append(" | ");
                                String dimension = ab.has("dimension") ? ab.get("dimension").asText() : "";
                                String abName = ab.has("name") ? ab.get("name").asText() : "";
                                def.append(dimension).append(": ").append(abName);
                            }
                        }
                    }
                    entry.put("definition", def.toString());

                    entityMap.put(name, entry);
                }
            }
        }
    }

    private void processLocations(JsonNode factJson, Map<String, Map<String, Object>> entityMap,
                                   Map<String, Set<Integer>> entityChapters, int chapterNum) {
        JsonNode node = factJson.get("locations");
        if (node != null && node.isArray()) {
            for (JsonNode loc : node) {
                String name = loc.has("name") ? loc.get("name").asText() : "";
                if (name.isEmpty()) continue;

                entityChapters.computeIfAbsent(name, k -> new HashSet<>()).add(chapterNum);

                if (!entityMap.containsKey(name)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", name);
                    entry.put("type", "location");
                    entry.put("category", "location");
                    entry.put("first_chapter", chapterNum);

                    // Build definition
                    String definition = "";
                    if (loc.has("description") && !loc.get("description").isNull()) {
                        definition = loc.get("description").asText();
                    } else if (loc.has("type") && !loc.get("type").isNull()) {
                        definition = loc.get("type").asText();
                    }
                    entry.put("definition", definition);

                    // Add parent if exists
                    if (loc.has("parent") && !loc.get("parent").isNull()) {
                        entry.put("parent", loc.get("parent").asText());
                    }

                    entityMap.put(name, entry);
                }
            }
        }
    }

    private void processItemEvents(JsonNode factJson, Map<String, Map<String, Object>> entityMap,
                                    Map<String, Set<Integer>> entityChapters, int chapterNum) {
        JsonNode node = factJson.get("item_events");
        if (node != null && node.isArray()) {
            for (JsonNode ie : node) {
                String name = ie.has("item_name") ? ie.get("item_name").asText() : "";
                if (name.isEmpty()) continue;

                entityChapters.computeIfAbsent(name, k -> new HashSet<>()).add(chapterNum);

                if (!entityMap.containsKey(name)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", name);
                    entry.put("type", "item");
                    entry.put("category", "item");
                    entry.put("first_chapter", chapterNum);

                    // Build definition
                    StringBuilder def = new StringBuilder();
                    if (ie.has("item_type") && !ie.get("item_type").isNull()) {
                        def.append(ie.get("item_type").asText());
                    }
                    if (ie.has("description") && !ie.get("description").isNull()) {
                        String desc = ie.get("description").asText();
                        if (def.length() > 0) def.append(" - ");
                        def.append(desc.length() > 50 ? desc.substring(0, 50) : desc);
                    }
                    entry.put("definition", def.toString());

                    entityMap.put(name, entry);
                }
            }
        }
    }

    private void processOrgEvents(JsonNode factJson, Map<String, Map<String, Object>> entityMap,
                                   Map<String, Set<Integer>> entityChapters, int chapterNum) {
        JsonNode node = factJson.get("org_events");
        if (node != null && node.isArray()) {
            for (JsonNode oe : node) {
                String name = oe.has("org_name") ? oe.get("org_name").asText() : "";
                if (name.isEmpty()) continue;

                entityChapters.computeIfAbsent(name, k -> new HashSet<>()).add(chapterNum);

                if (!entityMap.containsKey(name)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", name);
                    entry.put("type", "org");
                    entry.put("category", "org");
                    entry.put("first_chapter", chapterNum);

                    // Build definition
                    String definition = "";
                    if (oe.has("org_type") && !oe.get("org_type").isNull()) {
                        definition = oe.get("org_type").asText();
                    }
                    entry.put("definition", definition);

                    entityMap.put(name, entry);
                }
            }
        }
    }

    private void processConcepts(JsonNode factJson, Map<String, Map<String, Object>> entityMap,
                                  Map<String, Set<Integer>> entityChapters, int chapterNum,
                                  String categoryFilter) {
        JsonNode node = factJson.get("new_concepts");
        if (node != null && node.isArray()) {
            for (JsonNode nc : node) {
                String name = nc.has("name") ? nc.get("name").asText() : "";
                if (name.isEmpty()) continue;

                String cat = nc.has("category") ? nc.get("category").asText() : "其他";

                // Filter by specific concept sub-category
                if (categoryFilter != null && !categoryFilter.isEmpty() &&
                        !Arrays.asList("person", "location", "item", "org", "concept").contains(categoryFilter)) {
                    if (!cat.equals(categoryFilter)) continue;
                }

                entityChapters.computeIfAbsent(name, k -> new HashSet<>()).add(chapterNum);

                if (!entityMap.containsKey(name)) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", name);
                    entry.put("type", "concept");
                    entry.put("category", cat);
                    entry.put("first_chapter", chapterNum);

                    // Build definition
                    String definition = "";
                    if (nc.has("definition") && !nc.get("definition").isNull()) {
                        definition = nc.get("definition").asText();
                        if (definition.length() > 100) {
                            definition = definition.substring(0, 100);
                        }
                    }
                    entry.put("definition", definition);

                    entityMap.put(name, entry);
                }
            }
        }
    }

    /**
     * 获取概念详情
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getConceptDetail(
            @PathVariable String novelId,
            @PathVariable String name) {

        Map<String, Object> conceptInfo = null;
        List<Map<String, Object>> excerpts = new ArrayList<>();
        Set<String> relatedConcepts = new HashSet<>();

        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);

        for (Chapter chapter : chapters) {
            var factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());

            if (factOpt.isEmpty()) {
                continue;
            }

            ChapterFact fact = factOpt.get();
            int chapterNum = chapter.getChapterNum();

            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());

                JsonNode conceptsNode = factJson.get("new_concepts");
                if (conceptsNode != null && conceptsNode.isArray()) {
                    for (JsonNode nc : conceptsNode) {
                        if (nc.has("name") && name.equals(nc.get("name").asText())) {
                            if (conceptInfo == null) {
                                conceptInfo = new LinkedHashMap<>();
                                conceptInfo.put("name", name);
                                conceptInfo.put("category", nc.has("category") ? nc.get("category").asText() : "其他");
                                conceptInfo.put("definition", nc.has("definition") ? nc.get("definition").asText() : "");
                                conceptInfo.put("first_chapter", chapterNum);
                            }

                            // Collect related concepts
                            if (nc.has("related")) {
                                JsonNode related = nc.get("related");
                                if (related.isArray()) {
                                    for (JsonNode r : related) {
                                        String relName = r.asText();
                                        if (!relName.isEmpty() && !relName.equals(name)) {
                                            relatedConcepts.add(relName);
                                        }
                                    }
                                }
                            }

                            // Add excerpt
                            if (nc.has("definition") && !nc.get("definition").isNull()) {
                                Map<String, Object> excerpt = new HashMap<>();
                                excerpt.put("chapter", chapterNum);
                                excerpt.put("text", nc.get("definition").asText());
                                excerpts.add(excerpt);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}", chapter.getId());
            }
        }

        if (conceptInfo == null) {
            return ResponseEntity.notFound().build();
        }

        conceptInfo.put("excerpts", excerpts.size() > 5 ? excerpts.subList(0, 5) : excerpts);
        conceptInfo.put("related_concepts", new ArrayList<>(relatedConcepts));

        return ResponseEntity.ok(conceptInfo);
    }
}
