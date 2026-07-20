package com.bullboard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArticleImageUploadRequest {

    @NotBlank
    @Size(max = 255)
    private String originalFileName;

    @NotBlank
    private String contentType;

    @Min(1)
    @Max(5 * 1024 * 1024)
    private long fileSize;

    public String getOriginalFileName() { return originalFileName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
}
