package com.aireader.v2.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 章节事实实体类
 * 存储LLM提取的章节分析数据
 */
@Entity
@Table(name = "chapter_facts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"novel_id", "chapter_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(name = "fact_json", nullable = false, columnDefinition = "TEXT")
    private String factJson;

    @Column(name = "llm_model")
    private String llmModel;

    @Column(name = "extracted_at")
    @Builder.Default
    private LocalDateTime extractedAt = LocalDateTime.now();

    @Column(name = "extraction_ms")
    private Integer extractionMs;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "cost_usd")
    private Double costUsd;

    @Column(name = "cost_cny")
    private Double costCny;

    @Column(name = "is_truncated")
    @Builder.Default
    private Boolean isTruncated = false;

    @Column(name = "segment_count")
    @Builder.Default
    private Integer segmentCount = 1;

    @Column(name = "scenes_json", columnDefinition = "TEXT")
    private String scenesJson;
}
