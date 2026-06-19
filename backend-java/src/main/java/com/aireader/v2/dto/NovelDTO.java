package com.aireader.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小说数据传输对象
 * 字段名使用snake_case以匹配前端期望
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelDTO {
    
    private String id;
    private String title;
    private String author;
    
    @JsonProperty("file_hash")
    private String fileHash;
    
    @JsonProperty("total_chapters")
    private Integer totalChapters;
    
    @JsonProperty("total_words")
    private Integer totalWords;
    
    @JsonProperty("analyzed_chapters")
    private Integer analyzedChapters;
    
    @JsonProperty("prescan_status")
    private String prescanStatus;
    
    @JsonProperty("is_sample")
    private Boolean isSample;
    
    private String synopsis;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("last_opened")
    private LocalDateTime lastOpened;
    
    @JsonProperty("analysis_progress")
    @Builder.Default
    private Integer analysisProgress = 0;
    
    @JsonProperty("failed_count")
    @Builder.Default
    private Integer failedCount = 0;
    
    @JsonProperty("reading_progress")
    @Builder.Default
    private Double readingProgress = 0.0;
}
