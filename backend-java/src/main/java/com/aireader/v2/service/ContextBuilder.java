package com.aireader.v2.service;

import com.aireader.v2.repository.ChapterFactRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 上下文摘要构建服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextBuilder {

    private final ChapterFactRepository chapterFactRepository;
    private final ObjectMapper objectMapper;

    /**
     * 构建指定章节之前的上下文摘要
     */
    public String buildContext(String novelId, int chapterNum, int contextChapters) {
        List<String> allCharacters = new ArrayList<>();
        List<String> allLocations = new ArrayList<>();

        // 获取前N章的事实
        for (int i = Math.max(1, chapterNum - contextChapters); i < chapterNum; i++) {
            try {
                List<com.aireader.v2.model.entity.ChapterFact> facts = 
                    chapterFactRepository.findByNovelIdAndChapterId(novelId, i);
                for (var fact : facts) {
                    Map<String, Object> factData = objectMapper.readValue(fact.getFactJson(), Map.class);
                    List<?> chars = (List<?>) factData.getOrDefault("characters", Collections.emptyList());
                    List<?> locs = (List<?>) factData.getOrDefault("locations", Collections.emptyList());
                    
                    for (Object ch : chars) {
                        String name = String.valueOf(ch);
                        if (!allCharacters.contains(name)) {
                            allCharacters.add(name);
                        }
                    }
                    for (Object loc : locs) {
                        String name = String.valueOf(loc);
                        if (!allLocations.contains(name)) {
                            allLocations.add(name);
                        }
                    }
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse fact JSON for chapter {}: {}", i, e.getMessage());
            }
        }

        StringBuilder context = new StringBuilder();
        if (!allCharacters.isEmpty()) {
            context.append("人物：").append(String.join("、", allCharacters)).append("\n");
        }
        if (!allLocations.isEmpty()) {
            context.append("地点：").append(String.join("、", allLocations)).append("\n");
        }

        return context.toString();
    }

    /**
     * 使用默认上下文章节数（3章）
     */
    public String buildContext(String novelId, int chapterNum) {
        return buildContext(novelId, chapterNum, 3);
    }
}