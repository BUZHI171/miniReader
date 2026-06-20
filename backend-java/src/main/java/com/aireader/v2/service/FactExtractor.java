package com.aireader.v2.service;

import com.aireader.v2.llm.CloudLlmClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 章节事实提取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactExtractor {

    private final CloudLlmClient cloudLlmClient;
    private final ObjectMapper objectMapper;

    /**
     * 从章节文本中提取事实
     */
    public Map<String, Object> extract(String novelId, int chapterNum, String chapterText, String context) {
        String systemPrompt = "你是一位专业的小说分析助手。请分析小说章节内容，提取其中的人物、地点、关系和事件。";
        String userPrompt = buildPrompt(chapterText, context);
        
        try {
            CloudLlmClient.LlmResponse response = cloudLlmClient.generate(systemPrompt, userPrompt);
            if (response.getError() != null) {
                log.error("LLM error for chapter {}: {}", chapterNum, response.getError());
                return createEmptyFact();
            }
            return parseFactResponse(response.getContent());
        } catch (Exception e) {
            log.error("Failed to extract facts for chapter {}: {}", chapterNum, e.getMessage());
            return createEmptyFact();
        }
    }

    private String buildPrompt(String chapterText, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析以下小说章节内容，提取其中的人物、地点、关系和事件。\n\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("上下文信息（之前章节已出现的实体）：\n");
            prompt.append(context);
            prompt.append("\n\n");
        }
        
        prompt.append("章节内容：\n");
        prompt.append(chapterText);
        prompt.append("\n\n");
        prompt.append("请以 JSON 格式输出，包含以下字段：\n");
        prompt.append("{\n");
        prompt.append("  \"characters\": [\"人物1\", \"人物2\", ...],\n");
        prompt.append("  \"locations\": [\"地点1\", \"地点2\", ...],\n");
        prompt.append("  \"relationships\": [{\"subject\": \"人物A\", \"relation\": \"关系\", \"object\": \"人物B\"}, ...],\n");
        prompt.append("  \"events\": [\"事件描述1\", \"事件描述2\", ...]\n");
        prompt.append("}\n");
        prompt.append("请确保 JSON 格式正确，不要包含其他文字。");
        
        return prompt.toString();
    }

    private Map<String, Object> parseFactResponse(String response) {
        try {
            return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse fact response, returning empty fact: {}", e.getMessage());
            return createEmptyFact();
        }
    }

    private Map<String, Object> createEmptyFact() {
        Map<String, Object> fact = new HashMap<>();
        fact.put("characters", Collections.emptyList());
        fact.put("locations", Collections.emptyList());
        fact.put("relationships", Collections.emptyList());
        fact.put("events", Collections.emptyList());
        return fact;
    }
}