package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户阅读状态实体类
 * 对应Python的UserState数据模型
 */
@Entity
@Table(name = "user_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserState {

    @Id
    @Column(name = "novel_id")
    private String novelId;

    @JsonProperty("last_chapter")
    @Column(name = "last_chapter")
    private Integer lastChapter;

    @JsonProperty("scroll_position")
    @Column(name = "scroll_position")
    @Builder.Default
    private Double scrollPosition = 0.0;

    @JsonProperty("chapter_range")
    @Column(name = "chapter_range")
    private String chapterRange;

    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private String updatedAt;
}
