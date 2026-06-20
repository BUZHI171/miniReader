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

        // Track entities with their chapter counts
        Map<String, Map<String, Object>> entityMap = new HashMap<>();

        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);

        for (Chapter chapter : chapters) {
            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());

            if (factOpt.isEmpty()) {
                continue;
            }

            ChapterFact fact = factOpt.get();
            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());

                processCharacterEntities(factJson, entityMap, chapter.getChapterNum());
                processLocationEntities(factJson, entityMap, chapter.getChapterNum());
                processItemEntities(factJson, entityMap, chapter.getChapterNum());
                processOrgEntities(factJson, entityMap, chapter.getChapterNum());
                processConceptEntities(factJson, entityMap, chapter.getChapterNum());

            } catch (Exception e) {
                log.warn("解析章节事实失败: chapterId={}, error={}", chapter.getId(), e.getMessage());
            }
        }

        // Convert map to list
        entities.addAll(entityMap.values());

        // Filter by type if specified
        if (type != null && !type.isEmpty()) {
            entities.removeIf(e -> !type.equals(e.get("type")));
        }

        // Sort by chapter_count descending, then by name
        entities.sort((a, b) -> {
            int countA = (Integer) a.getOrDefault("chapter_count", 0);
            int countB = (Integer) b.getOrDefault("chapter_count", 0);
            if (countA != countB) {
                return countB - countA;
            }
            return ((String) a.get("name")).compareTo((String) b.get("name"));
        });

        result.put("entities", entities);
        result.put("total", entities.size());

        return ResponseEntity.ok(result);
    }

    private void processCharacterEntities(JsonNode factJson, Map<String, Map<String, Object>> entityMap, int chapterNum) {
        JsonNode charactersNode = factJson.get("characters");
        if (charactersNode != null && charactersNode.isArray()) {
            for (JsonNode charNode : charactersNode) {
                if (charNode.has("name")) {
                    String name = charNode.get("name").asText();
                    Map<String, Object> entity = entityMap.computeIfAbsent(name, k -> {
                        Map<String, Object> e = new HashMap<>();
                        e.put("name", name);
                        e.put("type", "person");
                        e.put("chapter_count", 0);
                        e.put("chapters", new HashSet<Integer>());
                        return e;
                    });
                    Set<Integer> chapters = (Set<Integer>) entity.get("chapters");
                    chapters.add(chapterNum);
                    entity.put("chapter_count", chapters.size());
                    if (!entity.containsKey("first_chapter") || (Integer) entity.get("first_chapter") > chapterNum) {
                        entity.put("first_chapter", chapterNum);
                    }
                }
            }
        }
    }

    private void processLocationEntities(JsonNode factJson, Map<String, Map<String, Object>> entityMap, int chapterNum) {
        JsonNode locationsNode = factJson.get("locations");
        if (locationsNode != null && locationsNode.isArray()) {
            for (JsonNode locNode : locationsNode) {
                if (locNode.has("name")) {
                    String name = locNode.get("name").asText();
                    Map<String, Object> entity = entityMap.computeIfAbsent(name, k -> {
                        Map<String, Object> e = new HashMap<>();
                        e.put("name", name);
                        e.put("type", "location");
                        e.put("chapter_count", 0);
                        e.put("chapters", new HashSet<Integer>());
                        return e;
                    });
                    Set<Integer> chapters = (Set<Integer>) entity.get("chapters");
                    chapters.add(chapterNum);
                    entity.put("chapter_count", chapters.size());
                    if (!entity.containsKey("first_chapter") || (Integer) entity.get("first_chapter") > chapterNum) {
                        entity.put("first_chapter", chapterNum);
                    }
                }
            }
        }
    }

    private void processItemEntities(JsonNode factJson, Map<String, Map<String, Object>> entityMap, int chapterNum) {
        JsonNode itemsNode = factJson.get("item_events");
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                if (itemNode.has("item_name")) {
                    String name = itemNode.get("item_name").asText();
                    Map<String, Object> entity = entityMap.computeIfAbsent(name, k -> {
                        Map<String, Object> e = new HashMap<>();
                        e.put("name", name);
                        e.put("type", "item");
                        e.put("chapter_count", 0);
                        e.put("chapters", new HashSet<Integer>());
                        return e;
                    });
                    Set<Integer> chapters = (Set<Integer>) entity.get("chapters");
                    chapters.add(chapterNum);
                    entity.put("chapter_count", chapters.size());
                    if (!entity.containsKey("first_chapter") || (Integer) entity.get("first_chapter") > chapterNum) {
                        entity.put("first_chapter", chapterNum);
                    }
                }
            }
        }
    }

    private void processOrgEntities(JsonNode factJson, Map<String, Map<String, Object>> entityMap, int chapterNum) {
        JsonNode orgsNode = factJson.get("org_events");
        if (orgsNode != null && orgsNode.isArray()) {
            for (JsonNode orgNode : orgsNode) {
                if (orgNode.has("org_name")) {
                    String name = orgNode.get("org_name").asText();
                    if (name.isEmpty()) continue;
                    Map<String, Object> entity = entityMap.computeIfAbsent(name, k -> {
                        Map<String, Object> e = new HashMap<>();
                        e.put("name", name);
                        e.put("type", "org");
                        e.put("chapter_count", 0);
                        e.put("chapters", new HashSet<Integer>());
                        return e;
                    });
                    Set<Integer> chapters = (Set<Integer>) entity.get("chapters");
                    chapters.add(chapterNum);
                    entity.put("chapter_count", chapters.size());
                    if (!entity.containsKey("first_chapter") || (Integer) entity.get("first_chapter") > chapterNum) {
                        entity.put("first_chapter", chapterNum);
                    }
                }
            }
        }
    }

    private void processConceptEntities(JsonNode factJson, Map<String, Map<String, Object>> entityMap, int chapterNum) {
        JsonNode conceptsNode = factJson.get("new_concepts");
        if (conceptsNode != null && conceptsNode.isArray()) {
            for (JsonNode conceptNode : conceptsNode) {
                if (conceptNode.has("name")) {
                    String name = conceptNode.get("name").asText();
                    Map<String, Object> entity = entityMap.computeIfAbsent(name, k -> {
                        Map<String, Object> e = new HashMap<>();
                        e.put("name", name);
                        e.put("type", "concept");
                        e.put("chapter_count", 0);
                        e.put("chapters", new HashSet<Integer>());
                        return e;
                    });
                    Set<Integer> chapters = (Set<Integer>) entity.get("chapters");
                    chapters.add(chapterNum);
                    entity.put("chapter_count", chapters.size());
                    if (!entity.containsKey("first_chapter") || (Integer) entity.get("first_chapter") > chapterNum) {
                        entity.put("first_chapter", chapterNum);
                    }
                }
            }
        }
    }

    @GetMapping("/{entityName}")
    public ResponseEntity<Map<String, Object>> getEntityDetail(
            @PathVariable String novelId,
            @PathVariable String entityName) {

        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        String entityType = null;

        // For person profile aggregation
        List<Map<String, Object>> aliases = new ArrayList<>();
        Set<String> seenAliases = new HashSet<>();
        List<Map<String, Object>> appearances = new ArrayList<>();
        List<Map<String, Object>> abilities = new ArrayList<>();
        Map<String, List<int[]>> rawRelations = new HashMap<>(); // other_person -> [(chapter, type, evidence)]
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> experiences = new ArrayList<>();
        Set<Integer> chapterSet = new HashSet<>();
        int firstChapter = 0;

        // For location profile aggregation
        String locationType = "";
        String parent = null;
        Set<String> children = new HashSet<>();
        List<Map<String, Object>> descriptions = new ArrayList<>();
        Map<String, List<Integer>> visitorMap = new HashMap<>();
        List<Map<String, Object>> locationEvents = new ArrayList<>();

        // For item profile aggregation
        String itemType = "";
        List<Map<String, Object>> flow = new ArrayList<>();
        Set<String> relatedItems = new HashSet<>();

        // For org profile aggregation
        String orgType = "";
        List<Map<String, Object>> memberEvents = new ArrayList<>();
        List<Map<String, Object>> orgRelations = new ArrayList<>();

        for (Chapter chapter : chapters) {
            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());

            if (factOpt.isEmpty()) {
                continue;
            }

            ChapterFact fact = factOpt.get();
            int chapterNum = chapter.getChapterNum();

            try {
                JsonNode factJson = objectMapper.readTree(fact.getFactJson());

                // Process characters (person type)
                JsonNode charactersNode = factJson.get("characters");
                if (charactersNode != null && charactersNode.isArray()) {
                    for (JsonNode charNode : charactersNode) {
                        if (charNode.has("name") && entityName.equals(charNode.get("name").asText())) {
                            if (entityType == null) {
                                entityType = "person";
                            }

                            chapterSet.add(chapterNum);
                            if (firstChapter == 0 || chapterNum < firstChapter) {
                                firstChapter = chapterNum;
                            }

                            // Process aliases
                            JsonNode aliasesNode = charNode.get("new_aliases");
                            if (aliasesNode != null && aliasesNode.isArray()) {
                                for (JsonNode aliasNode : aliasesNode) {
                                    String alias = aliasNode.asText();
                                    if (!seenAliases.contains(alias) && !alias.equals(entityName)) {
                                        seenAliases.add(alias);
                                        Map<String, Object> aliasEntry = new HashMap<>();
                                        aliasEntry.put("name", alias);
                                        aliasEntry.put("first_chapter", chapterNum);
                                        aliases.add(aliasEntry);
                                    }
                                }
                            }

                            // Process appearance
                            if (charNode.has("appearance") && !charNode.get("appearance").isNull()) {
                                String appearance = charNode.get("appearance").asText();
                                if (!appearance.isEmpty()) {
                                    Map<String, Object> appearanceEntry = new HashMap<>();
                                    appearanceEntry.put("chapters", Collections.singletonList(chapterNum));
                                    appearanceEntry.put("description", appearance);
                                    appearances.add(appearanceEntry);
                                }
                            }

                            // Process abilities
                            JsonNode abilitiesNode = charNode.get("abilities_gained");
                            if (abilitiesNode != null && abilitiesNode.isArray()) {
                                for (JsonNode abilityNode : abilitiesNode) {
                                    Map<String, Object> ability = new HashMap<>();
                                    ability.put("chapter", chapterNum);
                                    ability.put("dimension", abilityNode.has("dimension") ? abilityNode.get("dimension").asText() : "");
                                    ability.put("name", abilityNode.has("name") ? abilityNode.get("name").asText() : "");
                                    ability.put("description", abilityNode.has("description") ? abilityNode.get("description").asText() : "");
                                    abilities.add(ability);
                                }
                            }
                        }
                    }
                }

                // Process relationships
                JsonNode relationshipsNode = factJson.get("relationships");
                if (relationshipsNode != null && relationshipsNode.isArray()) {
                    for (JsonNode relNode : relationshipsNode) {
                        String personA = relNode.has("person_a") ? relNode.get("person_a").asText() : "";
                        String personB = relNode.has("person_b") ? relNode.get("person_b").asText() : "";
                        String other = null;

                        if (entityName.equals(personA)) {
                            other = personB;
                        } else if (entityName.equals(personB)) {
                            other = personA;
                        }

                        if (other != null && !other.equals(entityName)) {
                            String relationType = relNode.has("relation_type") ? relNode.get("relation_type").asText() : "";
                            String evidence = relNode.has("evidence") ? relNode.get("evidence").asText() : "";

                            rawRelations.computeIfAbsent(other, k -> new ArrayList<>())
                                    .add(new int[]{chapterNum, relationType.hashCode(), evidence.hashCode()});
                            // Store raw relation data for later processing
                            if (!rawRelations.containsKey(other + "_details")) {
                                rawRelations.put(other + "_details", new ArrayList<>());
                            }
                        }
                    }
                }

                // Process item_events for person items
                JsonNode itemEventsNode = factJson.get("item_events");
                if (itemEventsNode != null && itemEventsNode.isArray()) {
                    for (JsonNode itemNode : itemEventsNode) {
                        String actor = itemNode.has("actor") && !itemNode.get("actor").isNull() ? itemNode.get("actor").asText() : "";
                        String recipient = itemNode.has("recipient") && !itemNode.get("recipient").isNull() ? itemNode.get("recipient").asText() : "";

                        if (entityName.equals(actor) || entityName.equals(recipient)) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("chapter", chapterNum);
                            item.put("item_name", itemNode.has("item_name") ? itemNode.get("item_name").asText() : "");
                            item.put("item_type", itemNode.has("item_type") ? itemNode.get("item_type").asText() : "");
                            item.put("action", itemNode.has("action") ? itemNode.get("action").asText() : "");
                            item.put("description", itemNode.has("description") && !itemNode.get("description").isNull() ? itemNode.get("description").asText() : "");
                            items.add(item);
                        }
                    }
                }

                // Process events for person experiences
                JsonNode eventsNode = factJson.get("events");
                if (eventsNode != null && eventsNode.isArray()) {
                    for (JsonNode eventNode : eventsNode) {
                        JsonNode participantsNode = eventNode.get("participants");
                        if (participantsNode != null && participantsNode.isArray()) {
                            boolean hasEntity = false;
                            for (JsonNode p : participantsNode) {
                                if (entityName.equals(p.asText())) {
                                    hasEntity = true;
                                    break;
                                }
                            }
                            if (hasEntity) {
                                Map<String, Object> exp = new HashMap<>();
                                exp.put("chapter", chapterNum);
                                exp.put("summary", eventNode.has("summary") ? eventNode.get("summary").asText() : "");
                                exp.put("type", eventNode.has("type") ? eventNode.get("type").asText() : "");
                                exp.put("location", eventNode.has("location") && !eventNode.get("location").isNull() ? eventNode.get("location").asText() : null);
                                experiences.add(exp);
                            }
                        }
                    }
                }

                // Process locations
                JsonNode locationsNode = factJson.get("locations");
                if (locationsNode != null && locationsNode.isArray()) {
                    for (JsonNode locNode : locationsNode) {
                        if (locNode.has("name") && entityName.equals(locNode.get("name").asText())) {
                            if (entityType == null) {
                                entityType = "location";
                            }

                            chapterSet.add(chapterNum);

                            if (locNode.has("type") && locationType.isEmpty()) {
                                locationType = locNode.get("type").asText();
                            }

                            if (locNode.has("parent") && parent == null && !locNode.get("parent").isNull()) {
                                parent = locNode.get("parent").asText();
                            }

                            if (locNode.has("description") && !locNode.get("description").isNull()) {
                                Map<String, Object> desc = new HashMap<>();
                                desc.put("chapter", chapterNum);
                                desc.put("description", locNode.get("description").asText());
                                descriptions.add(desc);
                            }
                        }

                        // Check if this location is a child of the entity
                        if (locNode.has("parent") && entityName.equals(locNode.get("parent").asText())) {
                            children.add(locNode.get("name").asText());
                        }
                    }
                }

                // Process item_events for item profile
                if (itemEventsNode != null && itemEventsNode.isArray()) {
                    for (JsonNode itemNode : itemEventsNode) {
                        if (itemNode.has("item_name") && entityName.equals(itemNode.get("item_name").asText())) {
                            if (entityType == null) {
                                entityType = "item";
                            }

                            chapterSet.add(chapterNum);

                            if (itemNode.has("item_type") && itemType.isEmpty()) {
                                itemType = itemNode.get("item_type").asText();
                            }

                            Map<String, Object> flowEntry = new HashMap<>();
                            flowEntry.put("chapter", chapterNum);
                            flowEntry.put("action", itemNode.has("action") ? itemNode.get("action").asText() : "");
                            flowEntry.put("actor", itemNode.has("actor") && !itemNode.get("actor").isNull() ? itemNode.get("actor").asText() : null);
                            flowEntry.put("recipient", itemNode.has("recipient") && !itemNode.get("recipient").isNull() ? itemNode.get("recipient").asText() : null);
                            flowEntry.put("description", itemNode.has("description") && !itemNode.get("description").isNull() ? itemNode.get("description").asText() : "");
                            flow.add(flowEntry);
                        }
                    }
                }

                // Process org_events for org profile
                JsonNode orgEventsNode = factJson.get("org_events");
                if (orgEventsNode != null && orgEventsNode.isArray()) {
                    for (JsonNode orgNode : orgEventsNode) {
                        if (orgNode.has("org_name") && entityName.equals(orgNode.get("org_name").asText())) {
                            if (entityType == null) {
                                entityType = "org";
                            }

                            chapterSet.add(chapterNum);

                            if (orgNode.has("org_type") && orgType.isEmpty()) {
                                orgType = orgNode.get("org_type").asText();
                            }

                            if (orgNode.has("member") && !orgNode.get("member").isNull()) {
                                Map<String, Object> memberEvent = new HashMap<>();
                                memberEvent.put("chapter", chapterNum);
                                memberEvent.put("member", orgNode.get("member").asText());
                                memberEvent.put("role", orgNode.has("role") && !orgNode.get("role").isNull() ? orgNode.get("role").asText() : null);
                                memberEvent.put("action", orgNode.has("action") ? orgNode.get("action").asText() : "其他");
                                memberEvent.put("description", orgNode.has("description") && !orgNode.get("description").isNull() ? orgNode.get("description").asText() : "");
                                memberEvents.add(memberEvent);
                            }

                            if (orgNode.has("org_relation") && !orgNode.get("org_relation").isNull()) {
                                JsonNode orgRel = orgNode.get("org_relation");
                                if (orgRel.has("other_org")) {
                                    Map<String, Object> orgRelEntry = new HashMap<>();
                                    orgRelEntry.put("chapter", chapterNum);
                                    orgRelEntry.put("other_org", orgRel.get("other_org").asText());
                                    orgRelEntry.put("relation_type", orgRel.has("type") ? orgRel.get("type").asText() : "");
                                    orgRelations.add(orgRelEntry);
                                }
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

        Map<String, Object> result = new HashMap<>();
        result.put("name", entityName);
        result.put("type", entityType);

        // Build response based on entity type
        switch (entityType) {
            case "person":
                result.put("aliases", aliases);
                result.put("appearances", appearances);
                result.put("abilities", abilities);
                result.put("relations", buildRelations(rawRelations));
                result.put("items", items);
                result.put("experiences", experiences);
                result.put("stats", buildPersonStats(chapterSet, firstChapter, rawRelations));
                break;
            case "location":
                result.put("location_type", locationType);
                result.put("parent", parent);
                result.put("children", new ArrayList<>(children));
                result.put("siblings", new ArrayList<>()); // Would need world structure data
                result.put("descriptions", descriptions);
                result.put("visitors", buildVisitors(visitorMap));
                result.put("events", locationEvents);
                result.put("stats", buildLocationStats(chapterSet, visitorMap, locationEvents));
                break;
            case "item":
                result.put("item_type", itemType);
                result.put("flow", flow);
                result.put("related_items", new ArrayList<>(relatedItems));
                result.put("stats", buildItemStats(chapterSet, flow));
                break;
            case "org":
                result.put("org_type", orgType);
                result.put("member_events", memberEvents);
                result.put("org_relations", orgRelations);
                result.put("stats", buildOrgStats(chapterSet, memberEvents));
                break;
        }

        return ResponseEntity.ok(result);
    }

    private List<Map<String, Object>> buildRelations(Map<String, List<int[]>> rawRelations) {
        List<Map<String, Object>> relations = new ArrayList<>();

        for (Map.Entry<String, List<int[]>> entry : rawRelations.entrySet()) {
            String other = entry.getKey();
            if (other.endsWith("_details")) continue;

            List<int[]> stages = entry.getValue();
            if (stages.isEmpty()) continue;

            Map<String, Object> relation = new HashMap<>();
            relation.put("other_person", other);
            relation.put("category", "social"); // Default category
            relation.put("stages", new ArrayList<Map<String, Object>>());

            // Build stages (simplified - just collect chapters)
            Map<String, List<Integer>> typeChapters = new HashMap<>();
            for (int[] stage : stages) {
                // For simplicity, we just record the chapter
                typeChapters.computeIfAbsent("关联", k -> new ArrayList<>()).add(stage[0]);
            }

            List<Map<String, Object>> stagesList = new ArrayList<>();
            for (Map.Entry<String, List<Integer>> typeEntry : typeChapters.entrySet()) {
                Map<String, Object> stage = new HashMap<>();
                stage.put("chapters", typeEntry.getValue());
                stage.put("relation_type", typeEntry.getKey());
                stage.put("evidences", new ArrayList<String>());
                stage.put("evidence", "");
                stagesList.add(stage);
            }
            relation.put("stages", stagesList);

            relations.add(relation);
        }

        return relations;
    }

    private Map<String, Integer> buildPersonStats(Set<Integer> chapterSet, int firstChapter, Map<String, List<int[]>> rawRelations) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("chapter_count", chapterSet.size());
        stats.put("first_chapter", firstChapter);
        stats.put("last_chapter", chapterSet.isEmpty() ? 0 : Collections.max(chapterSet));
        stats.put("relation_count", rawRelations.size());
        return stats;
    }

    private List<Map<String, Object>> buildVisitors(Map<String, List<Integer>> visitorMap) {
        List<Map<String, Object>> visitors = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : visitorMap.entrySet()) {
            Map<String, Object> visitor = new HashMap<>();
            visitor.put("name", entry.getKey());
            visitor.put("chapters", entry.getValue());
            visitor.put("is_resident", entry.getValue().size() >= 3);
            visitors.add(visitor);
        }
        visitors.sort((a, b) -> ((List<Integer>) b.get("chapters")).size() - ((List<Integer>) a.get("chapters")).size());
        return visitors;
    }

    private Map<String, Integer> buildLocationStats(Set<Integer> chapterSet, Map<String, List<Integer>> visitorMap, List<Map<String, Object>> events) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("chapter_count", chapterSet.size());
        stats.put("first_chapter", chapterSet.isEmpty() ? 0 : Collections.min(chapterSet));
        stats.put("visitor_count", visitorMap.size());
        stats.put("event_count", events.size());
        return stats;
    }

    private Map<String, Integer> buildItemStats(Set<Integer> chapterSet, List<Map<String, Object>> flow) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("chapter_count", chapterSet.size());
        stats.put("first_chapter", chapterSet.isEmpty() ? 0 : Collections.min(chapterSet));
        stats.put("flow_count", flow.size());
        return stats;
    }

    private Map<String, Integer> buildOrgStats(Set<Integer> chapterSet, List<Map<String, Object>> memberEvents) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("chapter_count", chapterSet.size());
        stats.put("first_chapter", chapterSet.isEmpty() ? 0 : Collections.min(chapterSet));
        stats.put("member_event_count", memberEvents.size());
        return stats;
    }
}
