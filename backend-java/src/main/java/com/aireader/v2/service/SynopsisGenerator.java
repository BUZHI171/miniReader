package com.aireader.v2.service;

import com.aireader.v2.llm.CloudLlmClient;
import com.aireader.v2.repository.ChapterFactRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 小说概要生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SynopsisGenerator {

    private final ChapterFactRepository chapterFactRepository;
    private final CloudLlmClient cloudLlmClient;
    private final ObjectMapper objectMapper;

    /**
     * 自动生成小说概要
     */
    public String generate(String novelId) {
        // 收集关键信息
        Map<String, Object> keyInfo = collectKeyInformation(novelId);
        
        if (keyInfo.isEmpty()) {
            log.warn("No chapter facts found for novel {}", novelId);
            return "";
        }
        
        String prompt = buildPrompt(keyInfo);
        
        try {
            CloudLlmClient.LlmResponse response = cloudLlmClient.generate(
                "你是一位专业的文学编辑和小说评论家。请根据提供的小说信息，撰写一篇简洁而全面的小说概要。",
                prompt
            );
            
            if (response.getError() != null) {
                log.error("Failed to generate synopsis: {}", response.getError());
                return "";
            }
            
            return response.getContent();
        } catch (Exception e) {
            log.error("Failed to generate synopsis: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 收集关键信息（人物、地点、主要事件）
     */
    private Map<String, Object> collectKeyInformation(String novelId) {
        Map<String, Object> keyInfo = new HashMap<>();
        Set<String> characters = new LinkedHashSet<>();
        Set<String> locations = new LinkedHashSet<>();
        List<String> events = new ArrayList<>();
        
        List<com.aireader.v2.model.entity.ChapterFact> facts = chapterFactRepository.findByNovelId(novelId);
        for (var fact : facts) {
            try {
                Map<String, Object> factData = objectMapper.readValue(fact.getFactJson(), Map.class);
                
                List<?> chars = (List<?>) factData.getOrDefault("characters", Collections.emptyList());
                for (Object ch : chars) {
                    characters.add(String.valueOf(ch));
                }
                
                List<?> locs = (List<?>) factData.getOrDefault("locations", Collections.emptyList());
                for (Object loc : locs) {
                    locations.add(String.valueOf(loc));
                }
                
                List<?> evts = (List<?>) factData.getOrDefault("events", Collections.emptyList());
                for (Object evt : evts) {
                    events.add(String.valueOf(evt));
                }
                
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse fact JSON: {}", e.getMessage());
            }
        }
        
        keyInfo.put("characters", new ArrayList<>(characters));
        keyInfo.put("locations", new ArrayList<>(locations));
        keyInfo.put("events", events);
        
        return keyInfo;
    }

    /**
     * 构建生成概要的提示词
     */
    private String buildPrompt(Map<String, Object> keyInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下小说信息，撰写一篇完整的小说概要：\n\n");
        
        List<?> characters = (List<?>) keyInfo.get("characters");
        if (!characters.isEmpty()) {
            prompt.append("主要人物：\n");
            prompt.append(String.join("\n", characters.stream().map(Object::toString).toList()));
            prompt.append("\n\n");
        }
        
        List<?> locations = (List<?>) keyInfo.get("locations");
        if (!locations.isEmpty()) {
            prompt.append("主要地点：\n");
            prompt.append(String.join("\n", locations.stream().map(Object::toString).toList()));
            prompt.append("\n\n");
        }
        
        List<?> events = (List<?>) keyInfo.get("events");
        if (!events.isEmpty()) {
            prompt.append("主要事件：\n");
            prompt.append(String.join("\n", events.stream().map(Object::toString).toList()));
            prompt.append("\n\n");
        }
        
        prompt.append("请撰写一篇不少于200字的小说概要，包括故事背景、主要情节、人物关系和主题思想。");
        
        return prompt.toString();
    }
}