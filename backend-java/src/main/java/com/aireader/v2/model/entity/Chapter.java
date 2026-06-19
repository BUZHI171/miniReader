package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 章节实体类
 * 对应Python的Chapter数据模型
 */
@Entity
@Table(name = "chapters", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"novel_id", "chapter_num"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("id")
    @Column(name = "id")
    private Long id;

    @JsonProperty("novel_id")
    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @JsonProperty("chapter_num")
    @Column(name = "chapter_num", nullable = false)
    private Integer chapterNum;

    @JsonProperty("volume_num")
    @Column(name = "volume_num")
    private Integer volumeNum;

    @JsonProperty("volume_title")
    @Column(name = "volume_title")
    private String volumeTitle;

    @JsonProperty("title")
    @Column(name = "title", nullable = false)
    private String title;

    @JsonProperty("content")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @JsonProperty("word_count")
    @Column(name = "word_count")
    @Builder.Default
    private Integer wordCount = 0;

    @JsonProperty("analysis_status")
    @Column(name = "analysis_status")
    @Builder.Default
    private String analysisStatus = "pending";

    @JsonProperty("analyzed_at")
    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @JsonProperty("is_excluded")
    @Column(name = "is_excluded")
    @Builder.Default
    private Boolean isExcluded = false;

    @JsonProperty("analysis_error")
    @Column(name = "analysis_error", columnDefinition = "TEXT")
    private String analysisError;

    @JsonProperty("error_type")
    @Column(name = "error_type")
    private String errorType;

    @JsonProperty("updated_at")
    @Transient
    private String updatedAt;
}
