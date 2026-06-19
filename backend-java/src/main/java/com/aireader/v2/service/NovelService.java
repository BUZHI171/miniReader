package com.aireader.v2.service;

import com.aireader.v2.dto.NovelDTO;
import com.aireader.v2.dto.UploadPreviewResponse;
import com.aireader.v2.model.entity.Chapter;
import com.aireader.v2.model.entity.Novel;
import com.aireader.v2.repository.ChapterRepository;
import com.aireader.v2.repository.NovelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 小说服务类
 * 处理小说上传、导入、管理等业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NovelService {

    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有小说列表
     */
    public List<NovelDTO> listNovels() {
        List<Novel> novels = novelRepository.findAll();
        List<NovelDTO> result = new ArrayList<>();
        
        for (Novel novel : novels) {
            NovelDTO dto = convertToDTO(novel);
            result.add(dto);
        }
        
        return result;
    }

    /**
     * 根据ID获取小说
     */
    public Optional<Novel> getNovel(String novelId) {
        return novelRepository.findById(novelId);
    }

    /**
     * 上传并预览小说
     */
    @Transactional
    public UploadPreviewResponse uploadNovel(String filename, byte[] content) {
        // 验证文件格式
        String suffix = getFileExtension(filename);
        if (!suffix.equals(".txt") && !suffix.equals(".md")) {
            throw new IllegalArgumentException("不支持的文件格式 '" + suffix + "'，仅支持 .txt 和 .md");
        }

        // 验证文件大小
        if (content.length == 0) {
            throw new IllegalArgumentException("上传文件为空");
        }
        if (content.length > 100 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小超过 100MB 限制");
        }

        // 生成文件哈希
        String fileHash = generateFileHash(content);
        
        // 解析文本内容
        String text = new String(content);
        List<ChapterPreview> chapters = splitChapters(text);
        
        // 计算统计信息
        int totalWords = text.length();
        int totalChapters = chapters.size();
        
        // 返回预览信息
        UploadPreviewResponse response = new UploadPreviewResponse();
        response.setFileHash(fileHash);
        response.setTitle(extractTitle(filename));
        response.setTotalChapters(totalChapters);
        response.setTotalWords(totalWords);
        response.setChapters(chapters);
        
        return response;
    }

    /**
     * 确认导入小说
     */
    @Transactional
    public Novel confirmImport(String fileHash, String title, String author, 
                              byte[] content, List<Integer> excludedChapters) {
        // 生成小说ID
        String novelId = UUID.randomUUID().toString();
        
        // 解析章节
        String text = new String(content);
        List<ChapterPreview> chapterPreviews = splitChapters(text);
        
        // 创建小说实体
        Novel novel = Novel.builder()
                .id(novelId)
                .title(title)
                .author(author)
                .fileHash(fileHash)
                .totalChapters(chapterPreviews.size())
                .totalWords(text.length())
                .prescanStatus("pending")
                .isSample(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        novelRepository.save(novel);
        
        // 创建章节
        Set<Integer> excludedSet = excludedChapters != null ? 
                new HashSet<>(excludedChapters) : Collections.emptySet();
        
        for (int i = 0; i < chapterPreviews.size(); i++) {
            ChapterPreview preview = chapterPreviews.get(i);
            if (!excludedSet.contains(i + 1)) {
                Chapter chapter = Chapter.builder()
                        .novelId(novelId)
                        .chapterNum(i + 1)
                        .title(preview.getTitle())
                        .content(preview.getContent())
                        .wordCount(preview.getContent().length())
                        .analysisStatus("pending")
                        .isExcluded(false)
                        .build();
                chapterRepository.save(chapter);
            }
        }
        
        return novel;
    }

    /**
     * 删除小说
     */
    @Transactional
    public boolean deleteNovel(String novelId) {
        if (novelRepository.existsById(novelId)) {
            // 删除关联的章节
            List<Chapter> chapters = chapterRepository.findByNovelIdOrderByChapterNum(novelId);
            chapterRepository.deleteAll(chapters);
            
            // 删除小说本身
            novelRepository.deleteById(novelId);
            return true;
        }
        return false;
    }

    private NovelDTO convertToDTO(Novel novel) {
        NovelDTO dto = new NovelDTO();
        dto.setId(novel.getId());
        dto.setTitle(novel.getTitle());
        dto.setAuthor(novel.getAuthor());
        dto.setFileHash(novel.getFileHash());
        dto.setTotalChapters(novel.getTotalChapters());
        dto.setTotalWords(novel.getTotalWords());
        dto.setPrescanStatus(novel.getPrescanStatus());
        dto.setIsSample(novel.getIsSample());
        dto.setSynopsis(novel.getSynopsis());
        dto.setCreatedAt(novel.getCreatedAt());
        dto.setUpdatedAt(novel.getUpdatedAt());
        dto.setLastOpened(null); // TODO: 从用户状态获取
        
        // 统计已完成章节数
        Long completedCount = chapterRepository.countCompletedByNovelId(novel.getId());
        dto.setAnalyzedChapters(completedCount != null ? completedCount.intValue() : 0);
        
        // 计算分析进度
        if (novel.getTotalChapters() != null && novel.getTotalChapters() > 0) {
            int progress = (int) ((dto.getAnalyzedChapters() * 100.0) / novel.getTotalChapters());
            dto.setAnalysisProgress(progress);
        } else {
            dto.setAnalysisProgress(0);
        }
        
        // 统计失败章节数
        List<Chapter> failedChapters = chapterRepository.findFailedByNovelId(novel.getId());
        dto.setFailedCount(failedChapters != null ? failedChapters.size() : 0);
        
        return dto;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot).toLowerCase();
        }
        return "";
    }

    private String generateFileHash(byte[] content) {
        return UUID.nameUUIDFromBytes(content).toString();
    }

    private String extractTitle(String filename) {
        String name = filename;
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }
        return name;
    }

    private List<ChapterPreview> splitChapters(String text) {
        List<ChapterPreview> chapters = new ArrayList<>();
        
        // 简单的章节分割逻辑
        String[] lines = text.split("\n");
        StringBuilder currentContent = new StringBuilder();
        String currentTitle = "第1章";
        int chapterNum = 1;
        String chapterPattern = "^第[一二三四五六七八九十百千\\d]+章";
        
        for (String line : lines) {
            if (line.trim().matches(chapterPattern)) {
                // 保存之前的章节
                if (currentContent.length() > 0) {
                    chapters.add(new ChapterPreview(chapterNum, currentTitle, currentContent.toString()));
                    currentContent = new StringBuilder();
                }
                currentTitle = line.trim();
                chapterNum++;
            } else {
                currentContent.append(line).append("\n");
            }
        }
        
        // 添加最后一章
        if (currentContent.length() > 0) {
            chapters.add(new ChapterPreview(chapterNum, currentTitle, currentContent.toString()));
        }
        
        return chapters;
    }

    /**
     * 章节预览内部类
     */
    public static class ChapterPreview {
        private int number;
        private String title;
        private String content;
        private int wordCount;
        private String preview;

        public ChapterPreview(int number, String title, String content) {
            this.number = number;
            this.title = title;
            this.content = content;
            this.wordCount = content.length();
            this.preview = content.length() > 200 ? content.substring(0, 200) + "..." : content;
        }

        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public int getWordCount() { return wordCount; }
        public void setWordCount(int wordCount) { this.wordCount = wordCount; }
        public String getPreview() { return preview; }
        public void setPreview(String preview) { this.preview = preview; }
    }
}
