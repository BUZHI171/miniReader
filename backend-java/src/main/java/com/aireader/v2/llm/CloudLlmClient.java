package com.aireader.v2.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 浜慙LM瀹㈡埛绔? * 鏀寔OpenAI鍏煎API鍜孉nthropic Claude API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CloudLlmClient {

    private final ObjectMapper objectMapper;

    private String baseUrl = "https://api.openai.com";
    private String apiKey = "";
    private String model = "gpt-4o-mini";
    private String provider = "openai";

    /**
     * 閰嶇疆浜慙LM瀹㈡埛绔?     */
    public void configure(String baseUrl, String apiKey, String model, String provider) {
        this.baseUrl = baseUrl != null ? baseUrl : this.baseUrl;
        this.apiKey = apiKey != null ? apiKey : this.apiKey;
        this.model = model != null ? model : this.model;
        this.provider = provider != null ? provider : this.provider;
    }

    /**
     * 鐢熸垚鏂囨湰锛圤penAI鍏煎鏍煎紡锛?     */
    public LlmResponse generate(String system, String prompt) {
        if ("anthropic".equalsIgnoreCase(provider)) {
            return generateAnthropic(system, prompt);
        }
        return generateOpenAI(system, prompt);
    }

    /**
     * OpenAI鍏煎API鐢熸垚
     */
    private LlmResponse generateOpenAI(String system, String prompt) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", new Object[]{
                            Map.of("role", "system", "content", system),
                            Map.of("role", "user", "content", prompt)
                    },
                    "temperature", 0.1,
                    "max_tokens", 4096
            );

            String response = client.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            JsonNode json = objectMapper.readTree(response);
            JsonNode choices = json.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                JsonNode usage = json.get("usage");
                int promptTokens = usage != null ? usage.get("prompt_tokens").asInt(0) : 0;
                int completionTokens = usage != null ? usage.get("completion_tokens").asInt(0) : 0;

                return LlmResponse.builder()
                        .content(content)
                        .model(model)
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .build();
            }

            return LlmResponse.builder()
                    .content("")
                    .error("Invalid response format")
                    .build();

        } catch (Exception e) {
            log.error("OpenAI鐢熸垚澶辫触: {}", e.getMessage());
            return LlmResponse.builder()
                    .content("")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * Anthropic Claude API鐢熸垚
     */
    private LlmResponse generateAnthropic(String system, String prompt) {
        try {
            WebClient client = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("x-api-key", apiKey)
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "max_tokens", 4096,
                    "temperature", 0.1,
                    "system", system,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            String response = client.post()
                    .uri("/v1/messages")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            JsonNode json = objectMapper.readTree(response);
            JsonNode content = json.get("content");
            if (content != null && content.isArray() && content.size() > 0) {
                String text = content.get(0).get("text").asText();
                JsonNode usage = json.get("usage");
                int promptTokens = usage != null ? usage.get("input_tokens").asInt(0) : 0;
                int completionTokens = usage != null ? usage.get("output_tokens").asInt(0) : 0;

                return LlmResponse.builder()
                        .content(text)
                        .model(model)
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .build();
            }

            return LlmResponse.builder()
                    .content("")
                    .error("Invalid response format")
                    .build();

        } catch (Exception e) {
            log.error("Anthropic鐢熸垚澶辫触: {}", e.getMessage());
            return LlmResponse.builder()
                    .content("")
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 楠岃瘉API杩炴帴
     */
    public ValidationResult validate() {
        try {
            if (baseUrl == null || baseUrl.isEmpty()) {
                return new ValidationResult(false, "Base URL涓嶈兘涓虹┖");
            }

            boolean isLocal = baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
            if ((apiKey == null || apiKey.isEmpty()) && !isLocal) {
                return new ValidationResult(false, "API Key涓嶈兘涓虹┖");
            }

            WebClient.Builder builder = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader("Content-Type", "application/json");

            if ("anthropic".equalsIgnoreCase(provider)) {
                builder.defaultHeader("x-api-key", apiKey)
                       .defaultHeader("anthropic-version", "2023-06-01");
            } else {
                builder.defaultHeader("Authorization", "Bearer " + apiKey);
            }

            WebClient client = builder.build();

            String endpoint = "anthropic".equalsIgnoreCase(provider) ? "/v1/messages" : "/v1/models";
            String response = client.get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            return new ValidationResult(true, null);

        } catch (Exception e) {
            String error = e.getMessage();
            if (error != null && error.contains("Connection refused")) {
                return new ValidationResult(false, "鏃犳硶杩炴帴鍒?" + baseUrl);
            }
            if (error != null && error.contains("timeout")) {
                return new ValidationResult(false, "杩炴帴瓒呮椂");
            }
            return new ValidationResult(false, error != null ? error : "楠岃瘉澶辫触");
        }
    }

    /**
     * LLM鍝嶅簲鏁版嵁绫?     */
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
     * 楠岃瘉缁撴灉
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private String error;
    }
}