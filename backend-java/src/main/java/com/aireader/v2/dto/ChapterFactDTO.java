package com.aireader.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 章节事实数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterFactDTO {
    
    private Long chapterId;
    private String novelId;
    private List<CharacterFactDTO> characters;
    private List<RelationshipFactDTO> relationships;
    private List<LocationFactDTO> locations;
    private List<EventFactDTO> events;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterFactDTO {
        private String name;
        private List<String> newAliases;
        private String appearance;
        private List<AbilityDTO> abilitiesGained;
        private List<String> locationsInChapter;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbilityDTO {
        private String dimension;
        private String name;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipFactDTO {
        private String personA;
        private String personB;
        private String relationType;
        private Boolean isNew;
        private String previousType;
        private String evidence;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationFactDTO {
        private String name;
        private String type;
        private String parent;
        private String parentEvidence;
        private List<String> peers;
        private String description;
        private String role;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventFactDTO {
        private String summary;
        private String type;
        private String importance;
        private List<String> participants;
        private String location;
    }
}
