package com.aireader.v2.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 世界结构实体类
 * 存储小说的地理层级、空间关系等结构化数据
 */
@Entity
@Table(name = "world_structures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldStructure {

    @Id
    @Column(name = "novel_id")
    private String novelId;

    @Column(name = "structure_json", nullable = false, columnDefinition = "TEXT")
    private String structureJson;

    @Column(name = "source_chapters", columnDefinition = "TEXT")
    @Builder.Default
    private String sourceChapters = "[]";

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
