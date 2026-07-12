package com.bullboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ArticleCreateRequest {

    @NotNull
    private Long boardId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    public Long getBoardId() { return boardId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}
