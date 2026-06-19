package com.aireader.v2.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 章节分割工具类
 */
public class ChapterSplitter {

    // 常见章节标题模式
    private static final Pattern[] CHAPTER_PATTERNS = {
            Pattern.compile("^第[一二三四五六七八九十百千\\d]+章"),
            Pattern.compile("^Chapter\\s+\\d+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("^第[一二三四五六七八九十百千\\d]+回"),
            Pattern.compile("^\\d+\\.\\s+.+"),
            Pattern.compile("^[零一二三四五六七八九十百千\\d]+、.+")
    };

    /**
     * 分割章节
     */
    public static List<Chapter> split(String text) {
        List<Chapter> chapters = new ArrayList<>();
        String[] lines = text.split("\n");
        
        StringBuilder currentContent = new StringBuilder();
        String currentTitle = null;
        int chapterNum = 0;
        int lineInChapter = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            
            // 检查是否是章节标题
            if (isChapterTitle(trimmed)) {
                // 保存之前的章节
                if (currentTitle != null && currentContent.length() > 0) {
                    chapters.add(new Chapter(chapterNum, currentTitle, currentContent.toString()));
                    currentContent = new StringBuilder();
                }
                
                // 开始新章节
                currentTitle = trimmed;
                chapterNum++;
                lineInChapter = 0;
            } else {
                currentContent.append(line).append("\n");
                lineInChapter++;
            }
        }
        
        // 添加最后一章
        if (currentTitle != null && currentContent.length() > 0) {
            chapters.add(new Chapter(chapterNum, currentTitle, currentContent.toString()));
        }
        
        // 如果没有找到章节标题，将整个文本作为一章
        if (chapters.isEmpty() && text.length() > 0) {
            chapters.add(new Chapter(1, "第1章", text));
        }
        
        return chapters;
    }

    /**
     * 判断是否是章节标题
     */
    private static boolean isChapterTitle(String line) {
        if (line == null || line.length() < 2) {
            return false;
        }
        
        for (Pattern pattern : CHAPTER_PATTERNS) {
            if (pattern.matcher(line).find()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 章节内部类
     */
    public static class Chapter {
        private final int number;
        private final String title;
        private final String content;

        public Chapter(int number, String title, String content) {
            this.number = number;
            this.title = title;
            this.content = content;
        }

        public int getNumber() { return number; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public int getWordCount() { return content.length(); }
        public String getPreview() {
            if (content.length() <= 200) {
                return content;
            }
            return content.substring(0, 200) + "...";
        }
    }
}
