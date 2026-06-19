package com.aireader.v2.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Ollama本地LLM客户端
 * 对应Python的OllamaClient
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaClient {

    private final ObjectMapper objectMapper;
    
    @Value("${aireader.ollama.base-url:http://localhost:11434}")
    private String baseUrl;
    
    @Value("${aireader.ollama.timeout:600000}")
    private long timeout;

    /**
     * 生成文本
     */
    public LlmResponse generate(String model, String prompt) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(baseUrl)
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            );

            String response = client.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            JsonNode json = objectMapper.readTree(response);
            String text = json.has("response") ? json.get("response").asText() : "";

            return LlmResponse.builder()
                    .content(text)
                    .model(model)
                    .promptTokens(estimateTokens(prompt))
                    .completionTokens(estimateTokens(text))
                    .build();

        } catch (Exception e) {
            log.error("Ollama生成失败: {}", e.getMessage());
            return LlmResponse.builder()
                    .content("")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 流式生成文本
     */
    public CompletableFuture<LlmResponse> generateStream(String model, String prompt, 
                                                         LlmStreamCallback callback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                WebClient client = WebClient.builder()
                        .baseUrl(baseUrl)
                        .build();

                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "prompt", prompt,
                        "stream", true
                );

                StringBuilder fullContent = new StringBuilder();

                client.post()
                        .uri("/api/generate")
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofMillis(timeout))
                        .subscribe(
                                response -> {
                                    try {
                                        JsonNode json = objectMapper.readTree(response);
                                        String chunk = json.has("response") ? json.get("response").asText() : "";
                                        fullContent.append(chunk);
                                        callback.onChunk(chunk);
                                    } catch (Exception e) {
                                        log.error("解析流式响应失败: {}", e.getMessage());
                                    }
                                },
                                error -> {
                                    log.error("流式生成失败: {}", error.getMessage());
                                    callback.onError(error.getMessage());
                                },
                                () -> {
                                    callback.onComplete();
                                }
                        );

                return LlmResponse.builder()
                        .content(fullContent.toString())
                        .model(model)
                        .promptTokens(estimateTokens(prompt))
                        .completionTokens(estimateTokens(fullContent.toString()))
                        .build();

            } catch (Exception e) {
                log.error("Ollama流式生成失败: {}", e.getMessage());
                return LlmResponse.builder()
                        .content("")
                        .error(e.getMessage())
                        .build();
            }
        });
    }

    /**
     * 检查Ollama服务是否可用
     */
    public boolean isAvailable() {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(baseUrl)
                    .build();

            client.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return true;
        } catch (Exception e) {
            log.warn("Ollama服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 估算token数量（简单实现）
     */
    private int estimateTokens(String text) {
        // 简单估算：中文约2字符=1 token，英文约4字符=1 token
        int chineseChars = (int) text.chars().filter(c -> c > 0x4E00 && c < 0x9FA5).count();
        int otherChars = text.length() - chineseChars;
        return (int) Math.ceil(chineseChars / 2.0 + otherChars / 4.0);
    }

    /**
     * LLM响应数据类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LlmResponse {
        private String content;
        private String model;
        private int promptTokens;
        private int completionTokens;
        private String error;
    }

    /**
     * 流式回调接口
     */
    public interface LlmStreamCallback {
        void onChunk(String chunk);
        void onError(String error);
        void onComplete();
    }
}
