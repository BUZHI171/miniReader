package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 使用事件实体 - 匿名本地分析
 */
@Entity
@Table(name = "usage_events", indexes = {
    @Index(name = "idx_usage_event_type", columnList = "event_type"),
    @Index(name = "idx_usage_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageEvent {

    @Id
    @Column(name = "id")
    private Long id;

    @JsonProperty("event_type")
    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @JsonProperty("created_at")
    @Column(name = "created_at")
    private String createdAt;
}
