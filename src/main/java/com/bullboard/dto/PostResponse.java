package com.bullboard.dto;

import java.time.LocalDateTime;

public class PostResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorName;
    private final String boardName;
    private final LocalDateTime createdTime;

    public PostResponse(Long id, String title, String content,
                        String authorName, String boardName, LocalDateTime createdTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.boardName = boardName;
        this.createdTime = createdTime;
    }

    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
    public String getAuthorName() {
        return authorName;
    }
    public String getBoardName() {
        return boardName;
    }
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
}
