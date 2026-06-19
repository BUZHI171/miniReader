package com.aireader.v2.controller;

import com.aireader.v2.config.LlmConfig;
import com.aireader.v2.llm.OllamaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final LlmConfig llmConfig;
    private final OllamaClient ollamaClient;

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean ollamaAvailable = ollamaClient.isAvailable();
        
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "AI Reader V2 Backend",
                "version", "0.71.8",
                "llm_provider", llmConfig.getProvider(),
                "llm_model", llmConfig.getModel(),
                "ollama_available", ollamaAvailable
        ));
    }
}
