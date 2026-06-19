package com.aireader.v2.controller;

import com.aireader.v2.model.entity.Bookmark;
import com.aireader.v2.repository.BookmarkRepository;
import com.aireader.v2.repository.NovelRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final NovelRepository novelRepository;

    @Data
    public static class BookmarkRequest {
        @JsonProperty("chapter_num")
        private Integer chapterNum;

        @JsonProperty("scroll_position")
        private Double scrollPosition = 0.0;

        private String note = "";
    }

    // GET /api/novels/{novelId}/bookmarks
    @GetMapping("/api/novels/{novelId}/bookmarks")
    public ResponseEntity<Map<String, Object>> listBookmarks(@PathVariable String novelId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByNovelIdOrderByCreatedAtDesc(novelId);
        Map<String, Object> response = new HashMap<>();
        response.put("bookmarks", bookmarks);
        return ResponseEntity.ok(response);
    }

    // POST /api/novels/{novelId}/bookmarks
    @PostMapping("/api/novels/{novelId}/bookmarks")
    public ResponseEntity<Bookmark> createBookmark(
            @PathVariable String novelId,
            @RequestBody BookmarkRequest request) {

        if (!novelRepository.existsById(novelId)) {
            return ResponseEntity.notFound().build();
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Bookmark bookmark = Bookmark.builder()
                .novelId(novelId)
                .chapterNum(request.getChapterNum())
                .scrollPosition(request.getScrollPosition())
                .note(request.getNote())
                .createdAt(now)
                .build();

        bookmarkRepository.save(bookmark);
        return ResponseEntity.ok(bookmark);
    }

    // DELETE /api/bookmarks/{bookmarkId}
    @DeleteMapping("/api/bookmarks/{bookmarkId}")
    public ResponseEntity<Map<String, Boolean>> deleteBookmark(@PathVariable Long bookmarkId) {
        if (!bookmarkRepository.existsById(bookmarkId)) {
            return ResponseEntity.notFound().build();
        }

        bookmarkRepository.deleteById(bookmarkId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("ok", true);
        return ResponseEntity.ok(response);
    }
}
