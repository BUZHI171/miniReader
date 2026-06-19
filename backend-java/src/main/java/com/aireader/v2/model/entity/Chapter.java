package com.aireader.v2.model.entity;

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
    @Column(name = "id")
    private Long id;

    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @Column(name = "chapter_num", nullable = false)
    private Integer chapterNum;

    @Column(name = "volume_num")
    private Integer volumeNum;

    @Column(name = "volume_title")
    private String volumeTitle;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "word_count")
    @Builder.Default
    private Integer wordCount = 0;

    @Column(name = "analysis_status")
    @Builder.Default
    private String analysisStatus = "pending";

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @Column(name = "is_excluded")
    @Builder.Default
    private Boolean isExcluded = false;

    @Column(name = "analysis_error", columnDefinition = "TEXT")
    private String analysisError;

    @Column(name = "error_type")
    private String errorType;
}
