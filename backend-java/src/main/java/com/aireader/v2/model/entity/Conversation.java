package com.aireader.v2.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "novel_id", nullable = false)
    @JsonProperty("novel_id")
    private String novelId;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at")
    @JsonProperty("created_at")
    private String createdAt;

    @Column(name = "updated_at")
    @JsonProperty("updated_at")
    private String updatedAt;
}
