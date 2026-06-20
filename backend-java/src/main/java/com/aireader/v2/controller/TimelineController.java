package com.aireader.v2.controller;

import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.NovelRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Timeline data endpoint for visualization.
 */
@RestController
@RequestMapping("/api/novels/{novelId}/timeline")
@RequiredArgsConstructor
@Slf4j
public class TimelineController {

    private final NovelRepository novelRepository;
    private final ChapterFactRepository chapterFactRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get timeline data for a range of chapters.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTimeline(
            @PathVariable String novelId,
            @RequestParam(required = false, name = "chapter_start") Integer chapterStart,
            @RequestParam(required = false, name = "chapter_end") Integer chapterEnd) {

        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        // Get analyzed range
        int[] analyzedRange = getAnalyzedRange(novelId);
        int first = analyzedRange[0];
        int last = analyzedRange[1];

        if (first == 0) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("events", Collections.emptyList());
            empty.put("swimlanes", Collections.emptyMap());
            empty.put("analyzed_range", new int[]{0, 0});
            empty.put("suggested_hidden_types", Arrays.asList("角色登场", "物品交接"));
            empty.put("suggested_min_swimlane", 1);
            empty.put("total_swimlanes", 0);
            return ResponseEntity.ok(empty);
        }

        int start = chapterStart != null ? chapterStart : first;
        int end = chapterEnd != null ? chapterEnd : last;

        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        Map<String, Object> data = getTimelineData(novelId, start, end);
        data.put("analyzed_range", new int[]{first, last});
        
        return ResponseEntity.ok(data);
    }

    /**
     * Get the first and last analyzed chapter numbers.
     */
    private int[] getAnalyzedRange(String novelId) {
        List<ChapterFact> facts = chapterFactRepository.findByNovelId(novelId);
        if (facts.isEmpty()) {
            return new int[]{0, 0};
        }

        int first = Integer.MAX_VALUE;
        int last = 0;
        
        for (ChapterFact fact : facts) {
            int chId = fact.getChapterId().intValue();
            if (chId < first) first = chId;
            if (chId > last) last = chId;
        }
        
        return new int[]{first, last};
    }

    /**
     * Generate timeline data from chapter facts.
     */
    private Map<String, Object> getTimelineData(String novelId, int chapterStart, int chapterEnd) {
        List<Map<String, Object>> events = new ArrayList<>();
        Map<String, List<Integer>> swimlanes = new LinkedHashMap<>();
        Set<String> seenCharacters = new HashSet<>();

        int eventId = 0;

        // Load facts in range
        List<ChapterFact> facts = chapterFactRepository.findByNovelId(novelId);
        
        // Pre-compute character chapter counts
        Map<String, Set<Integer>> charChapters = new HashMap<>();
        for (ChapterFact fact : facts) {
            int chId = fact.getChapterId().intValue();
            if (chId < chapterStart || chId > chapterEnd) continue;
            
            try {
                Map<String, Object> factData = objectMapper.readValue(fact.getFactJson(), Map.class);
                List<Map<String, Object>> characters = (List<Map<String, Object>>) factData.getOrDefault("characters", Collections.emptyList());
                for (Map<String, Object> ch : characters) {
                    String name = String.valueOf(ch.get("name"));
                    charChapters.computeIfAbsent(name, k -> new HashSet<>()).add(chId);
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse fact JSON: {}", e.getMessage());
            }
        }

        for (ChapterFact fact : facts) {
            int chId = fact.getChapterId().intValue();
            if (chId < chapterStart || chId > chapterEnd) continue;

            try {
                Map<String, Object> factData = objectMapper.readValue(fact.getFactJson(), Map.class);

                // Process events
                List<Map<String, Object>> eventsList = (List<Map<String, Object>>) factData.getOrDefault("events", Collections.emptyList());
                for (Map<String, Object> ev : eventsList) {
                    String summary = String.valueOf(ev.getOrDefault("summary", ""));
                    String type = String.valueOf(ev.getOrDefault("type", "其他"));
                    String importance = String.valueOf(ev.getOrDefault("importance", "medium"));

                    List<Object> participantsList = (List<Object>) ev.getOrDefault("participants", Collections.emptyList());
                    List<String> participants = new ArrayList<>();
                    for (Object p : participantsList) {
                        if (p != null) participants.add(String.valueOf(p));
                    }

                    String location = ev.get("location") != null ? String.valueOf(ev.get("location")) : null;

                    Map<String, Object> event = createEvent(eventId++, chId, summary, type, importance, participants, location);
                    events.add(event);

                    for (String p : participants) {
                        swimlanes.computeIfAbsent(p, k -> new ArrayList<>()).add(eventId - 1);
                    }
                }

                // Process characters - first appearance
                List<Map<String, Object>> charList = (List<Map<String, Object>>) factData.getOrDefault("characters", Collections.emptyList());
                for (Map<String, Object> ch : charList) {
                    String name = String.valueOf(ch.get("name"));
                    
                    if (!seenCharacters.contains(name)) {
                        seenCharacters.add(name);
                        
                        // Only emit for characters appearing in >= 3 chapters
                        Set<Integer> appearances = charChapters.getOrDefault(name, Collections.emptySet());
                        if (appearances.size() >= 3) {
                            List<String> charNames = Collections.singletonList(name);
                            
                            List<Object> locations = (List<Object>) ch.get("locations_in_chapter");
                            String location = null;
                            if (locations != null && !locations.isEmpty() && locations.get(0) != null) {
                                location = String.valueOf(locations.get(0));
                            }
                            
                            Map<String, Object> event = createEvent(eventId++, chId, name + " 首次登场", 
                                "角色登场", "medium", charNames, location);
                            events.add(event);
                            swimlanes.computeIfAbsent(name, k -> new ArrayList<>()).add(eventId - 1);
                        }
                    }
                }

                // Process item events
                List<Map<String, Object>> itemEvents = (List<Map<String, Object>>) factData.getOrDefault("item_events", Collections.emptyList());
                for (Map<String, Object> ie : itemEvents) {
                    String action = String.valueOf(ie.getOrDefault("action", ""));
                    
                    // Skip noise actions
                    if (action.equals("出现") || action.equals("存在") || action.equals("提及")) {
                        continue;
                    }
                    
                    String actor = String.valueOf(ie.getOrDefault("actor", ""));
                    String itemName = String.valueOf(ie.getOrDefault("item_name", ""));
                    String recipient = ie.get("recipient") != null ? String.valueOf(ie.get("recipient")) : null;
                    
                    String summary = actor + " " + action + " " + itemName;
                    if (recipient != null && !recipient.isEmpty()) {
                        summary += " → " + recipient;
                    }
                    
                    List<String> participants = new ArrayList<>();
                    if (!actor.isEmpty()) participants.add(actor);
                    if (recipient != null && !recipient.isEmpty()) participants.add(recipient);
                    
                    Map<String, Object> event = createEvent(eventId++, chId, summary, 
                        "物品交接", "medium", participants, null);
                    events.add(event);
                }

                // Process relationships
                List<Map<String, Object>> relations = (List<Map<String, Object>>) factData.getOrDefault("relationships", Collections.emptyList());
                for (Map<String, Object> rel : relations) {
                    Boolean isNew = (Boolean) rel.get("is_new");
                    String previousType = rel.get("previous_type") != null ? String.valueOf(rel.get("previous_type")) : null;
                    String relationType = String.valueOf(rel.getOrDefault("relation_type", ""));
                    
                    if (Boolean.TRUE.equals(isNew) && previousType == null) {
                        String personA = String.valueOf(rel.getOrDefault("person_a", ""));
                        String personB = String.valueOf(rel.getOrDefault("person_b", ""));
                        String evidence = rel.get("evidence") != null ? String.valueOf(rel.get("evidence")) : null;
                        
                        String sum = personA + " 与 " + personB + " 建立" + relationType + "关系";
                        if (evidence != null && evidence.length() > 30) {
                            evidence = evidence.substring(0, 30);
                        }
                        if (evidence != null) {
                            sum += "（" + evidence + "）";
                        }
                        
                        List<String> relParticipants = Arrays.asList(personA, personB);
                        Map<String, Object> relEvent = createEvent(eventId++, chId, sum, 
                            "关系变化", "medium", relParticipants, null);
                        events.add(relEvent);
                    } else if (previousType != null && !previousType.equals(relationType)) {
                        String personA = String.valueOf(rel.getOrDefault("person_a", ""));
                        String personB = String.valueOf(rel.getOrDefault("person_b", ""));
                        
                        String sum = personA + " 与 " + personB + " 关系变化：" + previousType + "→" + relationType;
                        List<String> relParticipants = Arrays.asList(personA, personB);
                        Map<String, Object> relEvent = createEvent(eventId++, chId, sum, 
                            "关系变化", "high", relParticipants, null);
                        events.add(relEvent);
                    }
                }

            } catch (JsonProcessingException e) {
                log.warn("Failed to parse fact JSON for chapter {}: {}", chId, e.getMessage());
            }
        }

        // Compute suggested defaults
        int totalSwimlanes = swimlanes.size();
        List<String> suggestedHiddenTypes = Arrays.asList("角色登场", "物品交接");
        int suggestedMinSwimlane = totalSwimlanes > 100 ? 5 : totalSwimlanes > 30 ? 3 : 1;

        Map<String, Object> result = new HashMap<>();
        result.put("events", events);
        result.put("swimlanes", swimlanes);
        result.put("suggested_hidden_types", suggestedHiddenTypes);
        result.put("suggested_min_swimlane", suggestedMinSwimlane);
        result.put("total_swimlanes", totalSwimlanes);

        return result;
    }

    /**
     * Create an event entry.
     */
    private Map<String, Object> createEvent(int id, int chapter, String summary, 
            String type, String importance, List<String> participants, String location) {
        Map<String, Object> event = new HashMap<>();
        event.put("id", id);
        event.put("chapter", chapter);
        event.put("summary", summary);
        event.put("type", type);
        event.put("importance", importance);
        event.put("participants", participants);
        event.put("location", location);
        event.put("is_major", participants.size() >= 3);
        return event;
    }
}