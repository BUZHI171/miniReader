package com.aireader.v2.dto;

/**
 * Enumeration of analysis broadcast message types.
 */
public enum AnalysisMessageType {
    /** Progress update for current chapter */
    PROGRESS("progress"),
    
    /** Chapter is being processed */
    PROCESSING("processing"),
    
    /** Current stage label */
    STAGE("stage"),
    
    /** Chapter processing completed */
    CHAPTER_DONE("chapter_done"),
    
    /** Task status changed */
    TASK_STATUS("task_status"),
    
    /** Retry started */
    RETRY_START("retry_start"),
    
    /** Retry progress update */
    RETRY_PROGRESS("retry_progress"),
    
    /** Retry completed */
    RETRY_DONE("retry_done"),
    
    /** Hierarchy updated */
    HIERARCHY_UPDATED("hierarchy_updated");

    private final String value;

    AnalysisMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AnalysisMessageType fromValue(String value) {
        for (AnalysisMessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + value);
    }
}