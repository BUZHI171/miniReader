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
import java.util.LinkedHashMap;
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
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("novel_id", novelId);
        result.put("layers", new Object[0]);
        result.put("portals", new Object[0]);
        result.put("location_region_map", new LinkedHashMap<String, String>());
        result.put("location_layer_map", new LinkedHashMap<String, String>());
        result.put("location_parents", new LinkedHashMap<String, String>());
        result.put("location_tiers", new LinkedHashMap<String, String>());
        result.put("location_icons", new LinkedHashMap<String, String>());
        result.put("novel_genre_hint", null);
        result.put("spatial_scale", null);

        var structureOpt = worldStructureRepository.findByNovelId(novelId);

        if (structureOpt.isEmpty()) {
            return ResponseEntity.ok(result);
        }

        WorldStructure structure = structureOpt.get();

        try {
            JsonNode structureJson = objectMapper.readTree(structure.getStructureJson());

            // 提取 layers
            JsonNode layersNode = structureJson.get("layers");
            if (layersNode != null && layersNode.isArray()) {
                result.put("layers", objectMapper.convertValue(layersNode, Object[].class));
            }

            // 提取 portals
            JsonNode portalsNode = structureJson.get("portals");
            if (portalsNode != null && portalsNode.isArray()) {
                result.put("portals", objectMapper.convertValue(portalsNode, Object[].class));
            }

            // 提取 location_region_map
            JsonNode regionMapNode = structureJson.get("location_region_map");
            if (regionMapNode != null && regionMapNode.isObject()) {
                result.put("location_region_map", objectMapper.convertValue(regionMapNode, Map.class));
            }

            // 提取 location_layer_map
            JsonNode layerMapNode = structureJson.get("location_layer_map");
            if (layerMapNode != null && layerMapNode.isObject()) {
                result.put("location_layer_map", objectMapper.convertValue(layerMapNode, Map.class));
            }

            // 提取 location_parents
            JsonNode parentsNode = structureJson.get("location_parents");
            if (parentsNode != null && parentsNode.isObject()) {
                result.put("location_parents", objectMapper.convertValue(parentsNode, Map.class));
            }

            // 提取 location_tiers
            JsonNode tiersNode = structureJson.get("location_tiers");
            if (tiersNode != null && tiersNode.isObject()) {
                result.put("location_tiers", objectMapper.convertValue(tiersNode, Map.class));
            }

            // 提取 location_icons
            JsonNode iconsNode = structureJson.get("location_icons");
            if (iconsNode != null && iconsNode.isObject()) {
                result.put("location_icons", objectMapper.convertValue(iconsNode, Map.class));
            }

            // 提取 novel_genre_hint
            if (structureJson.has("novel_genre_hint")) {
                result.put("novel_genre_hint", structureJson.get("novel_genre_hint").asText(null));
            }

            // 提取 spatial_scale
            if (structureJson.has("spatial_scale")) {
                result.put("spatial_scale", structureJson.get("spatial_scale").asText(null));
            }

        } catch (Exception e) {
            log.error("解析世界结构JSON失败: {}", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
