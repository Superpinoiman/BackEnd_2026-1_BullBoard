package com.bullboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ArticleCreateRequest {

    @NotNull
    private Long boardId;

    @Pattern(
            regexp = "^$|^[A-Z][A-Z0-9.-]{0,9}$",
            message = "종목 심볼은 대문자 영문으로 시작해야 합니다."
    )
    private String symbol;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String content;

    @Size(max = 3)
    private List<String> imageKeys = new ArrayList<>();

    public Long getBoardId() { return boardId; }
    public String getSymbol() { return symbol; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<String> getImageKeys() { return imageKeys; }
}
