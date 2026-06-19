package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小说实体类
 * 对应Python的Novel数据模型
 */
@Entity
@Table(name = "novels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Novel {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author")
    private String author;

    @JsonProperty("file_hash")
    @Column(name = "file_hash")
    private String fileHash;

    @JsonProperty("total_chapters")
    @Column(name = "total_chapters")
    @Builder.Default
    private Integer totalChapters = 0;

    @JsonProperty("total_words")
    @Column(name = "total_words")
    @Builder.Default
    private Integer totalWords = 0;

    @JsonProperty("prescan_status")
    @Column(name = "prescan_status")
    @Builder.Default
    private String prescanStatus = "pending";

    @JsonProperty("is_sample")
    @Column(name = "is_sample")
    @Builder.Default
    private Boolean isSample = false;

    @Column(name = "synopsis", columnDefinition = "TEXT")
    private String synopsis;

    @JsonProperty("created_at")
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
