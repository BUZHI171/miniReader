package com.aireader.v2.dto;

import com.aireader.v2.service.NovelService.ChapterPreview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 上传预览响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadPreviewResponse {
    
    private String fileHash;
    private String title;
    private Integer totalChapters;
    private Integer totalWords;
    private List<ChapterPreview> chapters;
}
