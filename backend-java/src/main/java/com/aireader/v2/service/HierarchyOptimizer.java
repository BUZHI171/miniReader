package com.aireader.v2.service;

import com.aireader.v2.repository.ChapterFactRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 地点层级优化服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HierarchyOptimizer {

    private final ChapterFactRepository chapterFactRepository;
    private final ObjectMapper objectMapper;

    /**
     * 优化地点层级结构
     */
    public void optimize(String novelId) {
        // 收集所有地点
        Set<String> allLocations = collectAllLocations(novelId);
        
        // 构建层级关系
        Map<String, List<String>> hierarchy = buildHierarchy(allLocations);
        
        log.info("Optimized location hierarchy for novel {}: {} locations, {} parent-child relationships", 
                novelId, allLocations.size(), countRelationships(hierarchy));
    }

    /**
     * 收集小说中所有地点
     */
    private Set<String> collectAllLocations(String novelId) {
        Set<String> locations = new LinkedHashSet<>();
        
        List<com.aireader.v2.model.entity.ChapterFact> facts = chapterFactRepository.findByNovelId(novelId);
        for (var fact : facts) {
            try {
                Map<String, Object> factData = objectMapper.readValue(fact.getFactJson(), Map.class);
                List<?> locs = (List<?>) factData.getOrDefault("locations", Collections.emptyList());
                for (Object loc : locs) {
                    locations.add(String.valueOf(loc));
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse fact JSON: {}", e.getMessage());
            }
        }
        
        return locations;
    }

    /**
     * 构建地点层级关系
     */
    private Map<String, List<String>> buildHierarchy(Set<String> locations) {
        Map<String, List<String>> hierarchy = new HashMap<>();
        
        // 简单的层级推断：根据名称包含关系
        List<String> sortedLocations = new ArrayList<>(locations);
        sortedLocations.sort(Comparator.comparingInt(String::length));
        
        for (int i = 0; i < sortedLocations.size(); i++) {
            String smaller = sortedLocations.get(i);
            for (int j = i + 1; j < sortedLocations.size(); j++) {
                String larger = sortedLocations.get(j);
                if (larger.contains(smaller) && !smaller.equals(larger)) {
                    hierarchy.computeIfAbsent(larger, k -> new ArrayList<>()).add(smaller);
                }
            }
        }
        
        return hierarchy;
    }

    private int countRelationships(Map<String, List<String>> hierarchy) {
        return hierarchy.values().stream().mapToInt(List::size).sum();
    }
}