package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.ArticlePageResponse;
import com.bullboard.dto.MemberResponse;
import com.bullboard.dto.MemberUpdateRequest;
import com.bullboard.service.ArticleService;
import com.bullboard.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MyPageController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final SessionMemberResolver sessionMemberResolver;

    public MyPageController(MemberService memberService,
                            ArticleService articleService,
                            SessionMemberResolver sessionMemberResolver) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @GetMapping
    public ResponseEntity<MemberResponse> me(HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);

        return ResponseEntity.ok(new MemberResponse(memberService.getMember(memberId)));
    }

    @PutMapping
    public ResponseEntity<MemberResponse> updateMe(
            @Valid @RequestBody MemberUpdateRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);

        return ResponseEntity.ok(new MemberResponse(memberService.updateMember(memberId, request)));
    }

    @GetMapping("/articles")
    public ResponseEntity<ArticlePageResponse> myArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);
        return ResponseEntity.ok(
                articleService.getMyArticles(memberId, page, size));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMe(HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);

        memberService.deleteMember(memberId);
        sessionMemberResolver.logout(request);

        return ResponseEntity.noContent().build();
    }
}
