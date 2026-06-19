package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chapter_facts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterFact {

    @Id
    @Column(name = "id")
    private Long id;

    @JsonProperty("novel_id")
    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @JsonProperty("chapter_id")
    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @JsonProperty("fact_json")
    @Column(name = "fact_json", nullable = false, columnDefinition = "TEXT")
    private String factJson;

    @JsonProperty("llm_model")
    @Column(name = "llm_model")
    private String llmModel;

    @JsonProperty("extracted_at")
    @Column(name = "extracted_at")
    private String extractedAt;

    @JsonProperty("extraction_ms")
    @Column(name = "extraction_ms")
    private Integer extractionMs;

    @JsonProperty("input_tokens")
    @Column(name = "input_tokens")
    private Integer inputTokens;

    @JsonProperty("output_tokens")
    @Column(name = "output_tokens")
    private Integer outputTokens;

    @JsonProperty("cost_usd")
    @Column(name = "cost_usd")
    private Double costUsd;

    @JsonProperty("cost_cny")
    @Column(name = "cost_cny")
    private Double costCny;

    @JsonProperty("scenes_json")
    @Column(name = "scenes_json", columnDefinition = "TEXT")
    private String scenesJson;

    @JsonProperty("is_truncated")
    @Column(name = "is_truncated")
    @Builder.Default
    private Integer isTruncated = 0;

    @JsonProperty("segment_count")
    @Column(name = "segment_count")
    @Builder.Default
    private Integer segmentCount = 1;
}
