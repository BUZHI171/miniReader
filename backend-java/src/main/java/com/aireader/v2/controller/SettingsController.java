package com.aireader.v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("llm_provider", "ollama");
        settings.put("llm_model", "qwen3:8b");
        settings.put("ollama_base_url", "http://localhost:11434");
        settings.put("ollama_model", "qwen3:8b");
        settings.put("recommended_model", "qwen3:8b");
        settings.put("context_window", 8192);
        settings.put("llm_quality_review", false);

        Map<String, Object> result = new HashMap<>();
        result.put("settings", settings);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("llm_provider", "ollama");
        result.put("llm_model", "qwen3:8b");
        result.put("ollama_running", true);
        result.put("ollama_status", "running");
        result.put("ollama_url", "http://localhost:11434");
        result.put("model_available", true);
        result.put("recommended_model_installed", false);
        result.put("available_models", new ArrayList<>());
        result.put("api_available", false);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/hardware")
    public ResponseEntity<Map<String, Object>> getHardware() {
        Map<String, Object> result = new HashMap<>();
        result.put("total_ram_gb", 16.0);
        result.put("platform", "Windows");
        result.put("arch", "x86_64");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ollama/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        Map<String, Object> qwen4b = new HashMap<>();
        qwen4b.put("name", "qwen3:4b");
        qwen4b.put("display_name", "Qwen3 4B");
        qwen4b.put("size_gb", 2.5);
        qwen4b.put("min_ram_gb", 8);
        qwen4b.put("description", "杞婚噺妯″瀷锛岄€熷害蹇紝閫傚悎蹇€熷垎鏋?);
        qwen4b.put("recommended", false);
        qwen4b.put("installed", false);
        recommendations.add(qwen4b);

        Map<String, Object> qwen8b = new HashMap<>();
        qwen8b.put("name", "qwen3:8b");
        qwen8b.put("display_name", "Qwen3 8B");
        qwen8b.put("size_gb", 5.0);
        qwen8b.put("min_ram_gb", 16);
        qwen8b.put("description", "骞宠　璐ㄩ噺涓庨€熷害锛屾帹鑽愬ぇ澶氭暟鐢ㄦ埛浣跨敤");
        qwen8b.put("recommended", true);
        qwen8b.put("installed", true);
        recommendations.add(qwen8b);

        Map<String, Object> qwen14b = new HashMap<>();
        qwen14b.put("name", "qwen3:14b");
        qwen14b.put("display_name", "Qwen3 14B");
        qwen14b.put("size_gb", 9.0);
        qwen14b.put("min_ram_gb", 32);
        qwen14b.put("description", "鏈€浣冲垎鏋愯川閲忥紝閫傚悎楂橀厤鏈哄櫒");
        qwen14b.put("recommended", false);
        qwen14b.put("installed", false);
        recommendations.add(qwen14b);

        Map<String, Object> result = new HashMap<>();
        result.put("total_ram_gb", 16.0);
        result.put("recommendations", recommendations);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cloud/providers")
    public ResponseEntity<Map<String, Object>> getCloudProviders() {
        List<Map<String, Object>> providers = new ArrayList<>();

        Map<String, Object> deepseek = new HashMap<>();
        deepseek.put("id", "deepseek");
        deepseek.put("name", "DeepSeek");
        deepseek.put("base_url", "https://api.deepseek.com");
        deepseek.put("default_model", "deepseek-chat");
        deepseek.put("models", List.of("deepseek-chat", "deepseek-reasoner"));
        deepseek.put("api_format", "openai");
        providers.add(deepseek);

        Map<String, Object> openai = new HashMap<>();
        openai.put("id", "openai");
        openai.put("name", "OpenAI");
        openai.put("base_url", "https://api.openai.com/v1");
        openai.put("default_model", "gpt-4o-mini");
        openai.put("models", List.of("gpt-4o", "gpt-4o-mini", "gpt-4.1-mini"));
        openai.put("api_format", "openai");
        providers.add(openai);

        Map<String, Object> anthropic = new HashMap<>();
        anthropic.put("id", "anthropic");
        anthropic.put("name", "Anthropic锛圕laude锛?);
        anthropic.put("base_url", "https://api.anthropic.com");
        anthropic.put("default_model", "claude-sonnet-4-6");
        anthropic.put("models", List.of("claude-opus-4-7", "claude-sonnet-4-6", "claude-haiku-4-5"));
        anthropic.put("api_format", "anthropic");
        providers.add(anthropic);

        Map<String, Object> result = new HashMap<>();
        result.put("providers", providers);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cloud/config")
    public ResponseEntity<Map<String, Object>> getCloudConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("provider", "");
        result.put("base_url", "");
        result.put("model", "");
        result.put("has_api_key", false);
        result.put("api_key_masked", "");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cloud/validate")
    public ResponseEntity<Map<String, Object>> validateCloudApi(@RequestBody Map<String, Object> request) {
        String baseUrl = (String) request.getOrDefault("base_url", "");
        String apiKey = (String) request.getOrDefault("api_key", "");
        String provider = (String) request.getOrDefault("provider", "");

        Map<String, Object> result = new HashMap<>();
        if (baseUrl == null || baseUrl.isEmpty()) {
            result.put("valid", false);
            result.put("error", "Base URL 涓嶈兘涓虹┖");
            return ResponseEntity.ok(result);
        }

        boolean isLocal = baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1");
        if (apiKey == null || apiKey.isEmpty()) {
            if (!isLocal) {
                result.put("valid", false);
                result.put("error", "API Key 涓嶈兘涓虹┖");
                return ResponseEntity.ok(result);
            }
        }

        result.put("valid", true);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/budget")
    public ResponseEntity<Map<String, Object>> getBudget() {
        Map<String, Object> result = new HashMap<>();
        result.put("monthly_budget_cny", 0.0);
        result.put("monthly_used_cny", 0.0);
        result.put("monthly_used_usd", 0.0);
        result.put("monthly_input_tokens", 0);
        result.put("monthly_output_tokens", 0);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/restore-defaults")
    public ResponseEntity<Map<String, Object>> restoreDefaults() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}