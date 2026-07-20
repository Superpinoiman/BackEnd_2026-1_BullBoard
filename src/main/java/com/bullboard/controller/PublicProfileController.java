package com.bullboard.controller;

import com.bullboard.dto.ArticlePageResponse;
import com.bullboard.dto.PublicProfileResponse;
import com.bullboard.service.ArticleService;
import com.bullboard.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
public class PublicProfileController {

    private final MemberService memberService;
    private final ArticleService articleService;

    public PublicProfileController(MemberService memberService,
                                   ArticleService articleService) {
        this.memberService = memberService;
        this.articleService = articleService;
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<PublicProfileResponse> profile(
            @PathVariable Long memberId) {
        return ResponseEntity.ok(new PublicProfileResponse(
                memberService.getMember(memberId)));
    }

    @GetMapping("/{memberId}/articles")
    public ResponseEntity<ArticlePageResponse> articles(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        memberService.getMember(memberId);
        return ResponseEntity.ok(
                articleService.getPublicArticles(memberId, page, size));
    }
}
