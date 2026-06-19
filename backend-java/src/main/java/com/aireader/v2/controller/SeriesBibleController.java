package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.repository.NovelRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/novels/{novelId}/series-bible")
public class SeriesBibleController {

    private final NovelRepository novelRepository;

    public SeriesBibleController(NovelRepository novelRepository) {
        this.novelRepository = novelRepository;
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportSeriesBible(
            @PathVariable String novelId,
            @RequestBody(required = false) Map<String, Object> request) {

        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        String format = "markdown";
        String template = "complete";
        List<String> modules = null;
        Integer chapterStart = null;
        Integer chapterEnd = null;

        if (request != null) {
            format = (String) request.getOrDefault("format", "markdown");
            template = (String) request.getOrDefault("template", "complete");
            Object modulesObj = request.get("modules");
            if (modulesObj instanceof List) {
                modules = (List<String>) modulesObj;
            }
            Object cs = request.get("chapter_start");
            if (cs instanceof Number) {
                chapterStart = ((Number) cs).intValue();
            }
            Object ce = request.get("chapter_end");
            if (ce instanceof Number) {
                chapterEnd = ((Number) ce).intValue();
            }
        }

        String title = novel.getTitle() != null ? novel.getTitle() : "unknown";
        String rangeSuffix = "";
        if (chapterStart != null || chapterEnd != null) {
            int cs = chapterStart != null ? chapterStart : 1;
            int ce = chapterEnd != null ? chapterEnd : novel.getTotalChapters();
            rangeSuffix = "_Ch" + cs + "-" + ce;
        }

        StringBuilder md = new StringBuilder();
        md.append("# ").append(title).append(" 绯诲垪鍦ｇ粡\n\n");
        md.append("> 瀵煎嚭鏃堕棿: ").append(java.time.LocalDateTime.now()).append("\n\n");

        md.append("## 姒傝堪\n\n");
        md.append("- **浣滆€?*: ").append(novel.getAuthor() != null ? novel.getAuthor() : "鏈煡").append("\n");
        md.append("- **鎬荤珷鑺傛暟**: ").append(novel.getTotalChapters()).append("\n");
        md.append("- **鎬诲瓧鏁?*: ").append(novel.getTotalWords()).append("\n");
        md.append("- **棰勬壂鎻忕姸鎬?*: ").append(novel.getPrescanStatus()).append("\n\n");

        md.append("## 鐩綍\n\n");
        md.append("- [姒傝堪](#姒傝堪)\n");
        md.append("- [浜虹墿](#浜虹墿)\n");
        md.append("- [鍦扮偣](#鍦扮偣)\n");
        md.append("- [鍏崇郴](#鍏崇郴)\n");
        md.append("- [浜嬩欢](#浜嬩欢)\n\n");

        md.append("## 浜虹墿\n\n");
        md.append("锛堝緟鍒嗘瀽鏁版嵁锛塡n\n");

        md.append("## 鍦扮偣\n\n");
        md.append("锛堝緟鍒嗘瀽鏁版嵁锛塡n\n");

        md.append("## 鍏崇郴\n\n");
        md.append("锛堝緟鍒嗘瀽鏁版嵁锛塡n\n");

        md.append("## 浜嬩欢\n\n");
        md.append("锛堝緟鍒嗘瀽鏁版嵁锛塡n\n");

        String filename = title + "_" + template + rangeSuffix + ".md";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/markdown; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(md.toString().getBytes(StandardCharsets.UTF_8), headers, HttpStatus.OK);
    }

    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates(@PathVariable String novelId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<Map<String, Object>> templates = new ArrayList<>();

        Map<String, Object> complete = new HashMap<>();
        complete.put("id", "complete");
        complete.put("name", "瀹屾暣鐗?);
        complete.put("description", "鍖呭惈鎵€鏈夋ā鍧楃殑瀹屾暣璁惧畾闆?);
        complete.put("modules", List.of("overview", "characters", "locations", "relationships", "events", "timeline"));
        templates.add(complete);

        Map<String, Object> author = new HashMap<>();
        author.put("id", "author");
        author.put("name", "浣滆€呯増");
        author.put("description", "閫傚悎浣滆€呭弬鑰冪殑绮剧畝鐗?);
        author.put("modules", List.of("overview", "characters", "locations"));
        templates.add(author);

        Map<String, Object> result = new HashMap<>();
        result.put("templates", templates);

        return ResponseEntity.ok(result);
    }
}