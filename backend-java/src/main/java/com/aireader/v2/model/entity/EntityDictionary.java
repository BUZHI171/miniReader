package com.aireader.v2.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "entity_dictionary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityDictionary {

    @Id
    private Long id;

    @Column(name = "novel_id", nullable = false)
    private String novelId;

    @Column(name = "name", nullable = false)
    private String name;

    @JsonProperty("entity_type")
    @Column(name = "entity_type", nullable = false)
    @Builder.Default
    private String entityType = "unknown";

    @Column(name = "frequency", nullable = false)
    @Builder.Default
    private Integer frequency = 0;

    @Column(name = "confidence", nullable = false)
    @Builder.Default
    private String confidence = "medium";

    @Column(name = "aliases", columnDefinition = "TEXT")
    private String aliases;

    @Column(name = "source", nullable = false)
    private String source;

    @JsonProperty("sample_context")
    @Column(name = "sample_context")
    private String sampleContext;

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    static {
        OBJECT_MAPPER.findAndRegisterModules();
    }

    @Transient
    public List<String> getAliasesList() {
        if (aliases == null || aliases.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(aliases,
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setAliasesList(List<String> aliasesList) {
        try {
            this.aliases = OBJECT_MAPPER.writeValueAsString(aliasesList);
        } catch (Exception e) {
            this.aliases = "[]";
        }
    }
}