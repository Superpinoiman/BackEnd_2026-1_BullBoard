package com.bullboard.dto;

import com.bullboard.domain.Article;

import java.time.LocalDateTime;

public class TrendingArticleResponse {

    private final Long id;
    private final Long boardId;
    private final String boardName;
    private final String authorNickname;
    private final String title;
    private final String symbol;
    private final long likeCount;
    private final LocalDateTime createdDate;

    public TrendingArticleResponse(Article article, long likeCount) {
        this.id = article.getId();
        this.boardId = article.getBoard().getId();
        this.boardName = article.getBoard().getName();
        this.authorNickname = article.getAuthor() == null
                ? "알 수 없음" : article.getAuthor().getNickname();
        this.title = article.getTitle();
        this.symbol = article.getSymbol();
        this.likeCount = likeCount;
        this.createdDate = article.getCreatedDate();
    }

    public Long getId() { return id; }
    public Long getBoardId() { return boardId; }
    public String getBoardName() { return boardName; }
    public String getAuthorNickname() { return authorNickname; }
    public String getTitle() { return title; }
    public String getSymbol() { return symbol; }
    public long getLikeCount() { return likeCount; }
    public LocalDateTime getCreatedDate() { return createdDate; }
}
