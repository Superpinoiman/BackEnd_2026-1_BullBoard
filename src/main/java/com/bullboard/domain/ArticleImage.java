package com.bullboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "article_image")
public class ArticleImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(name = "object_key", nullable = false, unique = true, length = 500)
    private String objectKey;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    protected ArticleImage() {
    }

    public ArticleImage(Article article, String objectKey, String originalName,
                        String contentType, long fileSize, int sortOrder) {
        this.article = article;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.sortOrder = sortOrder;
        this.createdDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Article getArticle() { return article; }
    public String getObjectKey() { return objectKey; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public int getSortOrder() { return sortOrder; }
    public LocalDateTime getCreatedDate() { return createdDate; }
}
