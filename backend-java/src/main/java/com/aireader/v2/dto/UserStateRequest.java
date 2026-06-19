package com.aireader.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户状态请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStateRequest {
    
    private Integer last_chapter;
    
    private Double scroll_position = 0.0;
    
    private String chapter_range;
}
