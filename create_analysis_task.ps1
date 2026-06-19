$content = @'
package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分析任务实体类
 * 对应Python的AnalysisTask数据模型
 */
@Entity
@Table(name = "analysis_tasks", indexes = {
    @Index(name = "idx_analysis_novel", columnList = "novel_id, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTask {

    @Id
    @Column(name = "id")
    private String id;

    @JsonProperty("novel_id")
    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @JsonProperty("status")
    @Column(name = "status")
    @Builder.Default
    private String status = "pending";

    @JsonProperty("chapter_start")
    @Column(name = "chapter_start", nullable = false)
    private Integer chapterStart;

    @JsonProperty("chapter_end")
    @Column(name = "chapter_end", nullable = false)
    private Integer chapterEnd;

    @JsonProperty("current_chapter")
    @Column(name = "current_chapter")
    private Integer currentChapter;

    @JsonProperty("created_at")
    @Column(name = "created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private String updatedAt;

    @JsonProperty("timing_summary")
    @Column(name = "timing_summary", columnDefinition = "TEXT")
    private String timingSummary;
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\model\entity\AnalysisTask.java" -Value $content -Encoding UTF8