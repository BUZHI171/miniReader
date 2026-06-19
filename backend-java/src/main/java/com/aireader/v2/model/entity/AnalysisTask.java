package com.aireader.v2.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分析任务实体类
 * 对应Python的AnalysisTask数据模型
 */
@Entity
@Table(name = "analysis_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTask {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @Column(name = "status")
    @Builder.Default
    private String status = "pending";

    @Column(name = "chapter_start", nullable = false)
    private Integer chapterStart;

    @Column(name = "chapter_end", nullable = false)
    private Integer chapterEnd;

    @Column(name = "current_chapter")
    private Integer currentChapter;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "timing_summary", columnDefinition = "TEXT")
    private String timingSummary;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
