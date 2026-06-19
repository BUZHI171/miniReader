package com.aireader.v2.model.enums;

/**
 * 地点层级枚举
 * 对应Python的LocationTier
 */
public enum LocationTier {
    world("整个世界", 0),
    continent("大洲/大陆", 6),
    kingdom("国/大区域", 7),
    region("郡/山脉/海域", 8),
    city("城/镇/村/寺庙/门派", 9),
    site("具体地点", 10),
    building("建筑内部", 11);

    private final String description;
    private final int minZoom;

    LocationTier(String description, int minZoom) {
        this.description = description;
        this.minZoom = minZoom;
    }

    public String getDescription() {
        return description;
    }

    public int getMinZoom() {
        return minZoom;
    }
}
