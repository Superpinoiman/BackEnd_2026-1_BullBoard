package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.CommentRequest;
import com.bullboard.dto.CommentResponse;
import com.bullboard.service.CommentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;
    private final SessionMemberResolver sessionMemberResolver;

    public CommentController(CommentService commentService,
                             SessionMemberResolver sessionMemberResolver) {
        this.commentService = commentService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long articleId,
            HttpServletRequest request) {
        return ResponseEntity.ok(commentService.getComments(
                articleId, sessionMemberResolver.getMemberId(request)));
    }

    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long articleId,
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(articleId, request, memberId));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        return ResponseEntity.ok(commentService.updateComment(id, request, memberId));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        commentService.deleteComment(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
