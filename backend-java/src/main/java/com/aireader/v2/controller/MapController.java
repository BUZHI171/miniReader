package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.repository.NovelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/novels/{novelId}/map")
public class MapController {

    private final NovelRepository novelRepository;

    public MapController(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMap(
            @PathVariable String novelId,
            @RequestParam(required = false) Integer chapterStart,
            @RequestParam(required = false) Integer chapterEnd,
            @RequestParam(required = false) String layerId) {

        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "灏忚涓嶅瓨鍦?");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("locations", new ArrayList<>());
        result.put("trajectories", new HashMap<>());
        result.put("spatial_constraints", new ArrayList<>());
        result.put("layout", new ArrayList<>());
        result.put("layout_mode", "hierarchy");
        result.put("terrain_url", null);
        result.put("analyzed_range", new int[]{0, 0});
        result.put("rivers", new ArrayList<>());
        result.put("roads", new ArrayList<>());
        result.put("landmasses", new ArrayList<>());
        result.put("shelves", new ArrayList<>());
        result.put("region_boundaries", new ArrayList<>());
        result.put("portals", new ArrayList<>());
        result.put("revealed_location_names", new ArrayList<>());
        result.put("spatial_scale", "medium");
        result.put("layer_spatial_scales", new HashMap<>());
        result.put("canvas_size", new int[]{1000, 1000});
        result.put("geography_context", null);
        result.put("location_conflicts", new ArrayList<>());
        result.put("max_mention_count", 0);
        result.put("suggested_min_mentions", 1);
        result.put("geo_coords", new HashMap<>());
        result.put("world_structure", null);
        result.put("layer_layouts", new HashMap<>());
        result.put("quality_metrics", null);
        result.put("space_theme", false);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/layout/{locationName}")
    public ResponseEntity<Map<String, Object>> updateLocationOverride(
            @PathVariable String novelId,
            @PathVariable String locationName,
            @RequestBody Map<String, Object> body) {

        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "灏忚涓嶅瓨鍦?");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("message", "浣嶇疆宸蹭繚瀛?");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/terrain")
    public ResponseEntity<Map<String, Object>> getTerrain(@PathVariable String novelId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "灏忚涓嶅瓨鍦?");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error", "鍦板舰鍥惧皻鏈敓鎴?");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
