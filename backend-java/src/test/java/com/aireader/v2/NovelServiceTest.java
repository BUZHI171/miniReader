package com.aireader.v2;

import com.aireader.v2.dto.NovelDTO;
import com.aireader.v2.dto.UploadPreviewResponse;
import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.NovelRepository;
import com.aireader.v2.service.NovelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 小说服务测试
 */
@ExtendWith(MockitoExtension.class)
class NovelServiceTest {

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NovelService novelService;

    private Novel sampleNovel;

    @BeforeEach
    void setUp() {
        sampleNovel = Novel.builder()
                .id("test-id-123")
                .title("测试小说")
                .author("测试作者")
                .fileHash("abc123")
                .totalChapters(100)
                .totalWords(500000)
                .prescanStatus("completed")
                .isSample(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testListNovels() {
        when(novelRepository.findAll()).thenReturn(Arrays.asList(sampleNovel));
        when(chapterRepository.countCompletedByNovelId("test-id-123")).thenReturn(50L);

        List<NovelDTO> novels = novelService.listNovels();

        assertNotNull(novels);
        assertEquals(1, novels.size());
        assertEquals("测试小说", novels.get(0).getTitle());
        assertEquals("测试作者", novels.get(0).getAuthor());
        assertEquals(50, novels.get(0).getAnalyzedChapters());
    }

    @Test
    void testGetNovel() {
        when(novelRepository.findById("test-id-123")).thenReturn(Optional.of(sampleNovel));

        Optional<Novel> result = novelService.getNovel("test-id-123");

        assertTrue(result.isPresent());
        assertEquals("测试小说", result.get().getTitle());
    }

    @Test
    void testGetNovelNotFound() {
        when(novelRepository.findById("non-existent")).thenReturn(Optional.empty());

        Optional<Novel> result = novelService.getNovel("non-existent");

        assertFalse(result.isPresent());
    }

    @Test
    void testUploadNovelWithValidFile() {
        String filename = "test-novel.txt";
        String content = "第一章 测试章节\n这是第一章的内容。\n\n第二章 第二章节\n这是第二章的内容。";
        byte[] bytes = content.getBytes();

        UploadPreviewResponse response = novelService.uploadNovel(filename, bytes);

        assertNotNull(response);
        assertNotNull(response.getFileHash());
        assertEquals("test-novel", response.getTitle());
        assertEquals(2, response.getTotalChapters());
        assertTrue(response.getTotalWords() > 0);
    }

    @Test
    void testUploadNovelWithInvalidExtension() {
        String filename = "test.pdf";
        byte[] bytes = "content".getBytes();

        assertThrows(IllegalArgumentException.class, () -> {
            novelService.uploadNovel(filename, bytes);
        });
    }

    @Test
    void testUploadNovelWithEmptyFile() {
        String filename = "test.txt";
        byte[] bytes = new byte[0];

        assertThrows(IllegalArgumentException.class, () -> {
            novelService.uploadNovel(filename, bytes);
        });
    }

    @Test
    void testDeleteNovel() {
        when(novelRepository.existsById("test-id-123")).thenReturn(true);
        when(novelRepository.findByNovelIdOrderByChapterNum("test-id-123")).thenReturn(Arrays.asList());

        boolean result = novelService.deleteNovel("test-id-123");

        assertTrue(result);
        verify(novelRepository).deleteById("test-id-123");
    }

    @Test
    void testDeleteNovelNotFound() {
        when(novelRepository.existsById("non-existent")).thenReturn(false);

        boolean result = novelService.deleteNovel("non-existent");

        assertFalse(result);
        verify(novelRepository, never()).deleteById(any());
    }
}
