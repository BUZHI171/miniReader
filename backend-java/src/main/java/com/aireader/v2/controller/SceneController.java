package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.NovelRepository;
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

        Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
        
        if (factOpt.isPresent() && factOpt.get().getScenesJson() != null) {
            try {
                scenes = parseScenesFromJson(factOpt.get().getScenesJson(), chapterNum);
            } catch (Exception e) {
                log.warn("Failed to parse scenes_json: {}", e.getMessage());
                scenes = generateFallbackScenes(chapter, chapterNum);
            }
        } else {
            scenes = generateFallbackScenes(chapter, chapterNum);
        }

        ChapterScenesResponse response = new ChapterScenesResponse();
        response.setChapter(chapterNum);
        response.setScenes(scenes);
        response.setScene_count(scenes.size());
        response.setSource(factOpt.isPresent() && factOpt.get().getScenesJson() != null ? "llm" : "rule");
        
        return ResponseEntity.ok(response);
    }

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

        Map<Integer, List<Scene>> chaptersScenes = new TreeMap<>();
        int totalScenes = 0;

        for (int chNum = chapterStart; chNum <= chapterEnd; chNum++) {
            Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chNum);
            if (chapterOpt.isEmpty()) continue;

            Chapter chapter = chapterOpt.get();
            List<Scene> scenes;

            Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
            if (factOpt.isPresent() && factOpt.get().getScenesJson() != null) {
                try {
                    scenes = parseScenesFromJson(factOpt.get().getScenesJson(), chNum);
                } catch (Exception e) {
                    scenes = generateFallbackScenes(chapter, chNum);
                }
            } else {
                scenes = generateFallbackScenes(chapter, chNum);
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

    private List<Scene> generateFallbackScenes(Chapter chapter, int chapterNum) {
        List<Scene> scenes = new ArrayList<>();
        
        if (chapter.getContent() == null || chapter.getContent().isEmpty()) {
            return scenes;
        }

        String[] paragraphs = chapter.getContent().split("\n\n+");
        
        for (int i = 0; i < Math.min(paragraphs.length, 5); i++) {
            String para = paragraphs[i].trim();
            if (para.length() < 10) continue;
            
            Scene scene = new Scene();
            scene.setIndex(i);
            scene.setChapter(chapterNum);
            scene.setTitle("场景 " + (i + 1));
            scene.setLocation("");
            scene.setDescription(truncate(para, 200));
            scene.setCharacters(new ArrayList<>());
            scene.setKey_dialogue(new ArrayList<>());
            scene.setCharacter_roles(new ArrayList<>());
            scene.setDialogue_count(0);
            
            scenes.add(scene);
        }

        return scenes;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
