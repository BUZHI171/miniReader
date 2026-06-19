package com.aireader.v2.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "bookmarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "novel_id", nullable = false)
    @JsonProperty("novel_id")
    private String novelId;

    @Column(name = "chapter_num", nullable = false)
    @JsonProperty("chapter_num")
    private Integer chapterNum;

    @Column(name = "scroll_position")
    @JsonProperty("scroll_position")
    private Double scrollPosition;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private String createdAt;
}
