package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.ArticleCreateRequest;
import com.bullboard.dto.ArticlePageResponse;
import com.bullboard.dto.ArticleResponse;
import com.bullboard.dto.ArticleUpdateRequest;
import com.bullboard.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final SessionMemberResolver sessionMemberResolver;

    public ArticleController(ArticleService articleService,
                             SessionMemberResolver sessionMemberResolver) {
        this.articleService = articleService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody ArticleCreateRequest request,
                                                         HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(articleService.createArticle(request, memberId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id,
                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(articleService.getArticle(
                id, sessionMemberResolver.getMemberId(servletRequest)));
    }

    @GetMapping
    public ResponseEntity<ArticlePageResponse> getArticles(
            @RequestParam(required = false) Long boardId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String symbol,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(articleService.getArticles(
                boardId, keyword, symbol, sort, page, size,
                sessionMemberResolver.getMemberId(servletRequest)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);

        return ResponseEntity.ok(articleService.updateArticle(id, request, memberId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id,
                                              HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        articleService.deleteArticle(id, memberId);

        return ResponseEntity.noContent().build();
    }
}
