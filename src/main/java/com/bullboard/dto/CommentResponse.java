package com.bullboard.dto;

import com.bullboard.domain.Comment;

import java.time.LocalDateTime;

public class CommentResponse {

    private final Long id;
    private final Long articleId;
    private final Long authorId;
    private final String authorNickname;
    private final String content;
    private final LocalDateTime createdDate;
    private final LocalDateTime modifiedDate;
    private final boolean editable;

    public CommentResponse(Comment comment, Long loginMemberId) {
        this.id = comment.getId();
        this.articleId = comment.getArticle().getId();
        this.authorId = comment.getMember() == null ? null : comment.getMember().getId();
        this.authorNickname = comment.getMember() == null
                ? "알 수 없음" : comment.getMember().getNickname();
        this.content = comment.getContent();
        this.createdDate = comment.getCreatedDate();
        this.modifiedDate = comment.getModifiedDate();
        this.editable = authorId != null && loginMemberId != null
                && loginMemberId.equals(authorId);
    }

    public Long getId() { return id; }
    public Long getArticleId() { return articleId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorNickname() { return authorNickname; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public boolean isEditable() { return editable; }
}
