package com.bullboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ArticleUpdateRequest {

    @NotNull
    private Long boardId;

    @Pattern(regexp = "^$|^[A-Za-z][A-Za-z0-9.-]{0,9}$")
    private String symbol;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    public Long getBoardId() {
        return boardId;
    }
    public String getSymbol() {
        return symbol;
    }
    public String getTitle() {
        return title;
    }
    public String getContent() {
        return content;
    }
}
