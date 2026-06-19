$content = @'
package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 世界结构实体类
 * 对应Python的WorldStructure数据模型
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

    @JsonProperty("structure_json")
    @Column(name = "structure_json", nullable = false, columnDefinition = "TEXT")
    private String structureJson;

    @JsonProperty("source_chapters")
    @Column(name = "source_chapters", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String sourceChapters = "[]";

    @JsonProperty("created_at")
    @Column(name = "created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private String updatedAt;
}
'@

Set-Content -Path "e:\kaifa\other\miniReader\backend-java\src\main\java\com\aireader\v2\model\entity\WorldStructure.java" -Value $content -Encoding UTF8