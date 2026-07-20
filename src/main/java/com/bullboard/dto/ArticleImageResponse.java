package com.bullboard.dto;

import com.bullboard.domain.ArticleImage;

public class ArticleImageResponse {

    private final Long id;
    private final String url;
    private final String originalName;
    private final String contentType;
    private final long fileSize;
    private final int sortOrder;

    public ArticleImageResponse(ArticleImage image) {
        this.id = image.getId();
        this.url = "/article-images/" + image.getId();
        this.originalName = image.getOriginalName();
        this.contentType = image.getContentType();
        this.fileSize = image.getFileSize();
        this.sortOrder = image.getSortOrder();
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public int getSortOrder() { return sortOrder; }
}
