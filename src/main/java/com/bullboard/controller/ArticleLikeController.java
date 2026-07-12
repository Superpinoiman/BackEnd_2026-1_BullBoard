package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.ArticleLikeResponse;
import com.bullboard.service.ArticleLikeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{articleId}/likes")
public class ArticleLikeController {

    private final ArticleLikeService articleLikeService;
    private final SessionMemberResolver sessionMemberResolver;

    public ArticleLikeController(ArticleLikeService articleLikeService,
                                 SessionMemberResolver sessionMemberResolver) {
        this.articleLikeService = articleLikeService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @GetMapping
    public ResponseEntity<ArticleLikeResponse> getLike(
            @PathVariable Long articleId,
            HttpServletRequest request) {
        return ResponseEntity.ok(articleLikeService.getLike(
                articleId, sessionMemberResolver.getMemberId(request)));
    }

    @PostMapping
    public ResponseEntity<ArticleLikeResponse> like(
            @PathVariable Long articleId,
            HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);
        return ResponseEntity.ok(articleLikeService.like(articleId, memberId));
    }

    @DeleteMapping
    public ResponseEntity<ArticleLikeResponse> unlike(
            @PathVariable Long articleId,
            HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);
        return ResponseEntity.ok(articleLikeService.unlike(articleId, memberId));
    }
}
