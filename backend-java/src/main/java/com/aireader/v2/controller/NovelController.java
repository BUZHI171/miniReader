package com.aireader.v2.controller;

import com.aireader.v2.dto.NovelDTO;
import com.aireader.v2.dto.UploadPreviewResponse;
import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.service.NovelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
        } catch (IllegalArgumentException e) {
            log.error("上传失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取单个小说
     */
    @GetMapping("/{novelId}")
    public ResponseEntity<Novel> getNovel(@PathVariable String novelId) {
        return novelService.getNovel(novelId)
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
}
