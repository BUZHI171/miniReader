package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.NovelRepository;
import com.aireader.v2.service.SceneExtractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final SceneExtractor sceneExtractor;

    @Data
    public static class SceneCharacterRole {
        private String name;
        private String role;
    }

    @Data
    public static class Scene {
        private Integer index;
        private Integer chapter;
        private String title;
        private String location = "";
        private List<String> characters = new ArrayList<>();
        private String description = "";
        private Integer dialogue_count = 0;
        private List<Integer> paragraph_range;
        private String heading;
        private String time_of_day;
        private String emotional_tone;
        private List<String> key_dialogue = new ArrayList<>();
        private List<SceneCharacterRole> character_roles = new ArrayList<>();
        private String event_type;
        private String summary;
    }

    @Data
    public static class ChapterScenesResponse {
        private Integer chapter;
        private List<Scene> scenes;
        private Integer scene_count;
        private String source;
    }

    /**
     * Get scenes for a single chapter.
     * Priority: LLM-extracted scenes (DB) → rule-based fallback.
     */
    @GetMapping("/{chapterNum}")
    public ResponseEntity<ChapterScenesResponse> getChapterScenes(
            @PathVariable String novelId,
            @PathVariable Integer chapterNum) {

        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chapterNum);
        if (chapterOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Chapter chapter = chapterOpt.get();
        List<Scene> scenes = new ArrayList<>();
        String source = "rule";

        // Try LLM scenes first (stored in chapter_facts.scenes_json)
        Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());

//        if (factOpt.isPresent() && factOpt.get().getScenesJson() != null && !factOpt.get().getScenesJson().isEmpty()) {
//            try {
//                scenes = parseScenesFromJson(factOpt.get().getScenesJson(), chapterNum);
//                source = "llm";
//            } catch (Exception e) {
//                log.warn("Failed to parse scenes_json: {}", e.getMessage());
//            }
//        }

        // Fallback to rule-based extraction if no LLM scenes
        if (scenes.isEmpty() && chapter.getContent() != null) {
            Map<String, Object> factData = null;
            if (factOpt.isPresent() && factOpt.get().getFactJson() != null) {
                try {
                    factData = objectMapper.readValue(factOpt.get().getFactJson(), Map.class);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse fact_json: {}", e.getMessage());
                }
            }

            List<SceneExtractor.Scene> extractedScenes = sceneExtractor.extractScenes(
                chapter.getContent(),
                chapter.getTitle() != null ? chapter.getTitle() : "第" + chapterNum + "章",
                chapterNum,
                factData
            );

            scenes = convertScenes(extractedScenes);
        }

        ChapterScenesResponse response = new ChapterScenesResponse();
        response.setChapter(chapterNum);
        response.setScenes(scenes);
        response.setScene_count(scenes.size());
        response.setSource(source);

        return ResponseEntity.ok(response);
    }

    /**
     * Get scenes for a range of chapters.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getScenesRange(
            @PathVariable String novelId,
            @RequestParam(required = false) Integer chapterStart,
            @RequestParam(required = false) Integer chapterEnd) {

        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        if (chapterStart == null || chapterEnd == null) {
            chapterStart = 1;
            chapterEnd = 5;
        }

        Map<String, List<Scene>> chaptersScenes = new TreeMap<>();
        int totalScenes = 0;

        for (int chNum = chapterStart; chNum <= chapterEnd; chNum++) {
            Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chNum);
            if (chapterOpt.isEmpty()) continue;

            Chapter chapter = chapterOpt.get();
            List<Scene> scenes;
            String source = "rule";

            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            if (factOpt.isPresent() && factOpt.get().getScenesJson() != null && !factOpt.get().getScenesJson().isEmpty()) {
                try {
                    scenes = parseScenesFromJson(factOpt.get().getScenesJson(), chNum);
                    source = "llm";
                } catch (Exception e) {
                    scenes = extractRuleBasedScenes(chapter, chNum, factOpt.orElse(null));
                }
            } else {
                scenes = extractRuleBasedScenes(chapter, chNum, factOpt.orElse(null));
            }

            chaptersScenes.put(String.valueOf(chNum), scenes);
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
     * Extract scenes using rule-based algorithm.
     */
    private List<Scene> extractRuleBasedScenes(Chapter chapter, int chapterNum, ChapterFact fact) {
        Map<String, Object> factData = null;
        if (fact != null && fact.getFactJson() != null) {
            try {
                factData = objectMapper.readValue(fact.getFactJson(), Map.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse fact_json: {}", e.getMessage());
            }
        }

        List<SceneExtractor.Scene> extractedScenes = sceneExtractor.extractScenes(
            chapter.getContent(),
            chapter.getTitle() != null ? chapter.getTitle() : "第" + chapterNum + "章",
            chapterNum,
            factData
        );

        return convertScenes(extractedScenes);
    }

    /**
     * Convert SceneExtractor.Scene to controller Scene DTO.
     */
    private List<Scene> convertScenes(List<SceneExtractor.Scene> extractedScenes) {
        List<Scene> scenes = new ArrayList<>();
        for (SceneExtractor.Scene es : extractedScenes) {
            Scene scene = new Scene();
            scene.setIndex(es.getIndex());
            scene.setChapter(es.getChapter());
            scene.setTitle(es.getTitle());
            scene.setLocation(es.getLocation());
            scene.setCharacters(es.getCharacters());
            scene.setDescription(es.getDescription());
            scene.setDialogue_count(es.getDialogueCount());
            if (es.getParagraphRange() != null) {
                scene.setParagraph_range(Arrays.asList(es.getParagraphRange()[0], es.getParagraphRange()[1]));
            }
            scene.setHeading(es.getHeading());
            scene.setTime_of_day(es.getTimeOfDay());
            scene.setEmotional_tone(es.getEmotionalTone());
            scene.setKey_dialogue(es.getKeyDialogue());
            scene.setEvent_type(es.getEventType());
            scene.setSummary(es.getSummary());
            scenes.add(scene);
        }
        return scenes;
    }

    private List<Scene> parseScenesFromJson(String scenesJson, int chapterNum) throws JsonProcessingException {
        List<Scene> scenes = new ArrayList<>();
        JsonNode root = objectMapper.readTree(scenesJson);

        if (root.isArray()) {
            int index = 0;
            for (JsonNode node : root) {
                Scene scene = new Scene();
                scene.setIndex(index++);
                scene.setChapter(chapterNum);
                scene.setTitle(node.has("title") ? node.get("title").asText() : "场景 " + index);
                scene.setLocation(node.has("location") ? node.get("location").asText() : "");
                scene.setDescription(node.has("description") ? node.get("description").asText() : "");
                scene.setTime_of_day(node.has("time_of_day") ? node.get("time_of_day").asText() : "");
                scene.setEmotional_tone(node.has("emotional_tone") ? node.get("emotional_tone").asText() : "");
                scene.setEvent_type(node.has("event_type") ? node.get("event_type").asText() : "");
                scene.setSummary(node.has("summary") ? node.get("summary").asText() : "");
                scene.setDialogue_count(node.has("dialogue_count") ? node.get("dialogue_count").asInt() : 0);

                scene.setCharacters(new ArrayList<>());
                if (node.has("characters") && node.get("characters").isArray()) {
                    for (JsonNode charNode : node.get("characters")) {
                        scene.getCharacters().add(charNode.asText());
                    }
                }

                scene.setKey_dialogue(new ArrayList<>());
                if (node.has("key_dialogue") && node.get("key_dialogue").isArray()) {
                    for (JsonNode dNode : node.get("key_dialogue")) {
                        scene.getKey_dialogue().add(dNode.asText());
                    }
                }

                scene.setCharacter_roles(new ArrayList<>());
                if (node.has("character_roles") && node.get("character_roles").isArray()) {
                    for (JsonNode crNode : node.get("character_roles")) {
                        SceneCharacterRole cr = new SceneCharacterRole();
                        cr.setName(crNode.has("name") ? crNode.get("name").asText() : "");
                        cr.setRole(crNode.has("role") ? crNode.get("role").asText() : "");
                        scene.getCharacter_roles().add(cr);
                    }
                }

                scenes.add(scene);
            }
        }

        return scenes;
    }
}
