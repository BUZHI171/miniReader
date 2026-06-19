package com.aireader.v2.controller;

import com.aireader.v2.model.entity.EntityDictionary;
import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.repository.EntityDictionaryRepository;
import com.aireader.v2.repository.NovelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PrescanController {

    private final NovelRepository novelRepository;
    private final EntityDictionaryRepository entityDictionaryRepository;

    public PrescanController(NovelRepository novelRepository, EntityDictionaryRepository entityDictionaryRepository) {
        this.novelRepository = novelRepository;
        this.entityDictionaryRepository = entityDictionaryRepository;
    }

    @PostMapping("/novels/{novelId}/prescan")
    public ResponseEntity<Map<String, Object>> triggerPrescan(@PathVariable String novelId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "灏忚涓嶅瓨鍦?);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String status = novel.getPrescanStatus();
        if ("running".equals(status)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "棰勬壂鎻忔鍦ㄨ繘琛屼腑");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        if ("completed".equals(status)) {
            entityDictionaryRepository.deleteByNovelId(novelId);
        }

        novel.setPrescanStatus("running");
        novelRepository.save(novel);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "running");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/novels/{novelId}/prescan")
    public ResponseEntity<Map<String, Object>> getPrescanStatus(@PathVariable String novelId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "灏忚涓嶅瓨鍦?);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String status = novel.getPrescanStatus();
        long entityCount = entityDictionaryRepository.countByNovelId(novelId);

        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("entity_count", entityCount);
        result.put("created_at", novel.getCreatedAt() != null ? novel.getCreatedAt().toString() : null);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/novels/{novelId}/entity-dictionary")
    public ResponseEntity<Map<String, Object>> getEntityDictionary(
            @PathVariable String novelId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "100") int limit) {

        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "灏忚涓嶅瓨鍦?);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<EntityDictionary> entries;
        if (type != null && !type.isEmpty()) {
            entries = entityDictionaryRepository.findByNovelIdAndEntityTypeOrderByFrequencyDesc(novelId, type, limit);
        } else {
            entries = entityDictionaryRepository.findAllByNovelIdOrderByFrequencyDesc(novelId);
            if (entries.size() > limit) {
                entries = entries.subList(0, limit);
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (EntityDictionary entry : entries) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getName());
            item.put("entity_type", entry.getEntityType());
            item.put("frequency", entry.getFrequency());
            item.put("confidence", entry.getConfidence());
            item.put("aliases", entry.getAliasesList());
            item.put("source", entry.getSource());
            item.put("sample_context", entry.getSampleContext());
            data.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", data.size());
        return ResponseEntity.ok(result);
    }
}