package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.MemberRequest;
import com.bullboard.dto.MemberResponse;
import com.bullboard.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
public class MyPageController {

    private final MemberService memberService;
    private final SessionMemberResolver sessionMemberResolver;

    public MyPageController(MemberService memberService,
                            SessionMemberResolver sessionMemberResolver) {
        this.memberService = memberService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @GetMapping
    public ResponseEntity<MemberResponse> me(HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);

        return ResponseEntity.ok(new MemberResponse(memberService.getMember(memberId)));
    }

    @PutMapping
    public ResponseEntity<MemberResponse> updateMe(
            @Valid @RequestBody MemberRequest request,
            HttpServletRequest servletRequest) {
        Long memberId = sessionMemberResolver.requireMemberId(servletRequest);

        return ResponseEntity.ok(new MemberResponse(memberService.updateMember(memberId, request)));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMe(HttpServletRequest request) {
        Long memberId = sessionMemberResolver.requireMemberId(request);

        memberService.deleteMember(memberId);
        sessionMemberResolver.logout(request);

        return ResponseEntity.noContent().build();
    }
}
