package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.ArticleImageUploadRequest;
import com.bullboard.dto.ArticleImageUploadResponse;
import com.bullboard.service.ArticleImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article-images")
public class ArticleImageController {

    private final ArticleImageService articleImageService;
    private final SessionMemberResolver sessionMemberResolver;

    public ArticleImageController(ArticleImageService articleImageService,
                                  SessionMemberResolver sessionMemberResolver) {
        this.articleImageService = articleImageService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<ArticleImageUploadResponse> createUploadUrl(
            @Valid @RequestBody ArticleImageUploadRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(articleImageService.createUpload(memberId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> getImage(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(articleImageService.createDownloadUri(id))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id,
                                            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        articleImageService.deleteImage(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/uploads")
    public ResponseEntity<Void> deleteUnattachedUpload(
            @RequestParam String objectKey,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);
        articleImageService.deleteUnattachedUpload(memberId, objectKey);
        return ResponseEntity.noContent().build();
    }
}
