package com.bullboard.dto;

import com.bullboard.domain.Article;

import java.time.LocalDateTime;

public class ArticleResponse {

    private final Long id;
    private final Long boardId;
    private final String boardName;
    private final Long authorId;
    private final String authorNickname;
    private final String title;
    private final String content;
    private final String symbol;
    private final long viewCount;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;
    private final boolean editable;

    public ArticleResponse(Article article, Long loginMemberId) {
        this.id = article.getId();
        this.boardId = article.getBoard().getId();
        this.boardName = article.getBoard().getName();
        this.authorId = article.getAuthor() == null ? null : article.getAuthor().getId();
        this.authorNickname = article.getAuthor() == null
                ? "알 수 없음" : article.getAuthor().getNickname();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.symbol = article.getSymbol();
        this.viewCount = article.getViewCount();
        this.createdDate = article.getCreatedDate();
        this.modifiedDate = article.getModifiedDate();
        this.editable = authorId != null && loginMemberId != null
                && loginMemberId.equals(authorId);
    }

    public Long getId() { return id; }
    public Long getBoardId() { return boardId; }
    public String getBoardName() { return boardName; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorNickname() { return authorNickname; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSymbol() { return symbol; }
    public long getViewCount() { return viewCount; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public boolean isEditable() { return editable; }
}
