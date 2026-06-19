$content = @'
package com.aireader.v2.controller;

import com.aireader.v2.model.entity.WorldStructure;
import com.aireader.v2.repository.WorldStructureRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 世界结构控制器
 * 对应Python的WorldStructure API路由
 */
@RestController
@RequestMapping("/api/novels/{novelId}/world-structure")
@RequiredArgsConstructor
@Slf4j
public class WorldStructureController {

    private final WorldStructureRepository worldStructureRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取世界结构
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWorldStructure(@PathVariable String novelId) {
        Map<String, Object> result = new HashMap<>();
        
        var structureOpt = worldStructureRepository.findByNovelId(novelId);
        
        if (structureOpt.isEmpty()) {
            result.put("structure", null);
            result.put("exists", false);
            return ResponseEntity.ok(result);
        }
        
        WorldStructure structure = structureOpt.get();
        
        try {
            JsonNode structureJson = objectMapper.readTree(structure.getStructureJson());
            result.put("structure", structureJson);
            result.put("exists", true);
            result.put("source_chapters", objectMapper.readTree(structure.getSourceChapters()));
            result.put("updated_at", structure.getUpdatedAt());
        } catch (Exception e) {
            log.error("解析世界结构JSON失败: {}", e.getMessage());
            result.put("structure", structure.getStructureJson());
            result.put("exists", true);
        }
        
        return ResponseEntity.ok(result);
    }
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\controller\WorldStructureController.java" -Value $content -Encoding UTF8