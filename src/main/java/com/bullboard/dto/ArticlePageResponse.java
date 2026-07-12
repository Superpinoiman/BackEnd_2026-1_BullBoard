package com.bullboard.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class ArticlePageResponse {

    private final List<ArticleResponse> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public ArticlePageResponse(Page<ArticleResponse> articlePage) {
        this.content = articlePage.getContent();
        this.page = articlePage.getNumber();
        this.size = articlePage.getSize();
        this.totalElements = articlePage.getTotalElements();
        this.totalPages = articlePage.getTotalPages();
        this.first = articlePage.isFirst();
        this.last = articlePage.isLast();
    }

    public List<ArticleResponse> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
