package com.aireader.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小说数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelDTO {
    
    private String id;
    private String title;
    private String author;
    private String fileHash;
    private Integer totalChapters;
    private Integer totalWords;
    private Integer analyzedChapters;
    private String prescanStatus;
    private Boolean isSample;
    private String synopsis;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
