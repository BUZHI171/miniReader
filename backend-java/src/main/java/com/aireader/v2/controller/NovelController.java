package com.aireader.v2.controller;

import com.aireader.v2.dto.NovelDTO;
import com.aireader.v2.dto.UploadPreviewResponse;
import com.aireader.v2.dto.UserStateRequest;
import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.ChapterFact;
import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.model.entity.UserState;
import com.aireader.v2.repository.ChapterFactRepository;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.UserStateRepository;
import com.aireader.v2.service.NovelService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 小说管理控制器
 * 对应Python的Novels API路由
 */
@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
@Slf4j
public class NovelController {

    private final NovelService novelService;
    private final ChapterRepository chapterRepository;
    private final UserStateRepository userStateRepository;
    private final ChapterFactRepository chapterFactRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有小说列表
     */
    @GetMapping
    public ResponseEntity<Map<String, List<NovelDTO>>> listNovels() {
        List<NovelDTO> novels = novelService.listNovels();
        return ResponseEntity.ok(Map.of("novels", novels));
    }

    /**
     * 上传小说文件
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadPreviewResponse> uploadNovel(
            @RequestParam("file") MultipartFile file) {
        try {
            UploadPreviewResponse response = novelService.uploadNovel(
                    file.getOriginalFilename(),
                    file.getBytes()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IOException e) {
            log.error("上传失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取单个小说
     */
    @GetMapping("/{novelId}")
    public ResponseEntity<NovelDTO> getNovel(@PathVariable String novelId) {
        return novelService.getNovelDTO(novelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 获取小说章节列表
     */
    @GetMapping("/{novelId}/chapters")
    public ResponseEntity<Map<String, List<Chapter>>> getChapters(@PathVariable String novelId) {
        if (!novelService.getNovel(novelId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
        return ResponseEntity.ok(Map.of("chapters", chapters));
    }

    /**
     * 获取章节内容
     */
    @GetMapping("/{novelId}/chapters/{chapterNum}")
    public ResponseEntity<Chapter> getChapterContent(
            @PathVariable String novelId,
            @PathVariable Integer chapterNum) {
        return chapterRepository.findByNovelIdAndChapterNum(novelId, chapterNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 确认导入小说
     */
    @PostMapping("/confirm")
    public ResponseEntity<Novel> confirmImport(@RequestBody Map<String, Object> request) {
        try {
            String fileHash = (String) request.get("file_hash");
            String title = (String) request.get("title");
            String author = (String) request.get("author");
            @SuppressWarnings("unchecked")
            List<Integer> excludedChapters = (List<Integer>) request.get("excluded_chapters");
            
            // TODO: 从缓存获取文件内容
            Novel novel = novelService.confirmImport(fileHash, title, author, 
                    new byte[0], excludedChapters);
            return ResponseEntity.ok(novel);
        } catch (Exception e) {
            log.error("确认导入失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除小说
     */
    @DeleteMapping("/{novelId}")
    public ResponseEntity<Map<String, Boolean>> deleteNovel(@PathVariable String novelId) {
        boolean deleted = novelService.deleteNovel(novelId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("ok", true));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("ok", false));
    }

    /**
     * 获取小说统计信息
     */
    @GetMapping("/{novelId}/stats")
    public ResponseEntity<Map<String, Object>> getNovelStats(@PathVariable String novelId) {
        // TODO: 实现统计信息查询
        return ResponseEntity.ok(Map.of(
                "novel_id", novelId,
                "chapters", Map.of("total", 0, "analyzed", 0, "excluded", 0),
                "entities", Map.of("person", 0, "location", 0, "item", 0, "org", 0, "concept", 0, "total", 0)
        ));
    }

    /**
     * 获取用户阅读状态
     */
    @GetMapping("/{novelId}/user-state")
    public ResponseEntity<Map<String, Object>> getUserState(@PathVariable String novelId) {
        UserState state = userStateRepository.findByNovelId(novelId).orElse(null);
        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        if (state == null) {
            result.put("novel_id", novelId);
            result.put("last_chapter", null);
            result.put("scroll_position", 0.0);
            return ResponseEntity.ok(result);
        }
        result.put("novel_id", state.getNovelId());
        result.put("last_chapter", state.getLastChapter());
        result.put("scroll_position", state.getScrollPosition());
        result.put("chapter_range", state.getChapterRange());
        return ResponseEntity.ok(result);
    }

    /**
     * 保存用户阅读状态
     */
    @PutMapping("/{novelId}/user-state")
    public ResponseEntity<Map<String, Boolean>> saveUserState(
            @PathVariable String novelId,
            @RequestBody UserStateRequest request) {
        if (!novelService.getNovel(novelId).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        UserState state = userStateRepository.findByNovelId(novelId)
                .orElse(UserState.builder().novelId(novelId).build());
        
        state.setLastChapter(request.getLast_chapter());
        state.setScrollPosition(request.getScroll_position() != null ? request.getScroll_position() : 0.0);
        state.setChapterRange(request.getChapter_range());
        
        userStateRepository.save(state);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * 获取章节中的实体名称
     */
    @GetMapping("/{novelId}/chapters/{chapterNum}/entities")
    public ResponseEntity<Map<String, Object>> getChapterEntities(
            @PathVariable String novelId,
            @PathVariable Integer chapterNum) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<Chapter> chapterOpt = chapterRepository.findByNovelIdAndChapterNum(novelId, chapterNum);
        if (chapterOpt.isEmpty()) {
            result.put("entities", List.of());
            return ResponseEntity.ok(result);
        }
        
        Chapter chapter = chapterOpt.get();
        Optional<ChapterFact> factOpt = chapterFactRepository.findByNovelIdAndChapterId(novelId, chapter.getId());
        
        if (factOpt.isEmpty()) {
            result.put("entities", List.of());
            return ResponseEntity.ok(result);
        }
        
        ChapterFact fact = factOpt.get();
        try {
            JsonNode factJson = objectMapper.readTree(fact.getFactJson());
            JsonNode charactersNode = factJson.get("characters");
            
            List<Map<String, String>> entities = new ArrayList<>();
            if (charactersNode != null && charactersNode.isArray()) {
                for (JsonNode characterNode : charactersNode) {
                    Map<String, String> entity = new HashMap<>();
                    if (characterNode.has("name")) {
                        entity.put("name", characterNode.get("name").asText());
                    }
                    entity.put("type", "person");
                    entities.add(entity);
                }
            }
            
            JsonNode locationsNode = factJson.get("locations");
            if (locationsNode != null && locationsNode.isArray()) {
                for (JsonNode locationNode : locationsNode) {
                    Map<String, String> entity = new HashMap<>();
                    if (locationNode.has("name")) {
                        entity.put("name", locationNode.get("name").asText());
                    }
                    entity.put("type", "location");
                    entities.add(entity);
                }
            }
            
            result.put("entities", entities);
        } catch (Exception e) {
            log.error("解析章节事实失败: {}", e.getMessage());
            result.put("entities", List.of());
        }
        
        return ResponseEntity.ok(result);
    }
}
