package com.aireader.v2.model.enums;

/**
 * 地图层级类型枚举
 * 对应Python的LayerType
 */
public enum LayerType {
    overworld("主世界"),
    underground("地下"),
    sky("天空"),
    sea("海洋"),
    pocket("口袋空间"),
    spirit("灵界"),
    underwater("水下");

    private final String description;

    LayerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
