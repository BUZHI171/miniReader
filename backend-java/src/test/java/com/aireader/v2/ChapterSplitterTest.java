package com.aireader.v2;

import com.aireader.v2.util.ChapterSplitter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 章节分割器测试
 */
class ChapterSplitterTest {

    @Test
    void testSplitWithChineseChapterTitles() {
        String text = """
                第一章 测试章节
                这是第一章的内容。
                这里有很多文字。
                
                第二章 另一个章节
                这是第二章的内容。
                更多内容在这里。
                
                第三章 最后一章
                这是第三章的内容。
                """;

        List<ChapterSplitter.Chapter> chapters = ChapterSplitter.split(text);

        assertEquals(3, chapters.size());
        
        assertEquals(1, chapters.get(0).getNumber());
        assertEquals("第一章 测试章节", chapters.get(0).getTitle());
        assertTrue(chapters.get(0).getContent().contains("第一章"));
        
        assertEquals(2, chapters.get(1).getNumber());
        assertEquals("第二章 另一个章节", chapters.get(1).getTitle());
        
        assertEquals(3, chapters.get(2).getNumber());
        assertEquals("第三章 最后一章", chapters.get(2).getTitle());
    }

    @Test
    void testSplitWithEnglishChapterTitles() {
        String text = """
                Chapter 1 Introduction
                This is the first chapter.
                
                Chapter 2 Main Content
                This is the second chapter.
                
                Chapter 3 Conclusion
                This is the third chapter.
                """;

        List<ChapterSplitter.Chapter> chapters = ChapterSplitter.split(text);

        assertEquals(3, chapters.size());
        assertEquals(1, chapters.get(0).getNumber());
        assertEquals("Chapter 1 Introduction", chapters.get(0).getTitle());
    }

    @Test
    void testSplitWithoutChapterTitles() {
        String text = "这是一段没有章节标题的文本。\n第二行文本。\n第三行文本。";

        List<ChapterSplitter.Chapter> chapters = ChapterSplitter.split(text);

        assertEquals(1, chapters.size());
        assertEquals(1, chapters.get(0).getNumber());
        assertEquals("第1章", chapters.get(0).getTitle());
    }

    @Test
    void testChapterWordCount() {
        String text = """
                第一章 测试
                这是内容。
                """;

        List<ChapterSplitter.Chapter> chapters = ChapterSplitter.split(text);

        assertEquals(1, chapters.size());
        assertTrue(chapters.get(0).getWordCount() > 0);
    }

    @Test
    void testChapterPreview() {
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longContent.append("这是一行很长的文本内容。\n");
        }

        String text = "第一章 长章节\n" + longContent;

        List<ChapterSplitter.Chapter> chapters = ChapterSplitter.split(text);

        assertEquals(1, chapters.size());
        assertTrue(chapters.get(0).getPreview().length() <= 210); // 200 + "..."
    }
}
