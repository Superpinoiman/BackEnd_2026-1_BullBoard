package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.domain.Member;
import com.bullboard.dto.LoginRequest;
import com.bullboard.dto.MemberResponse;
import com.bullboard.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final MemberService memberService;
    private final SessionMemberResolver sessionMemberResolver;

    public AuthController(MemberService memberService,
                          SessionMemberResolver sessionMemberResolver) {
        this.memberService = memberService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest servletRequest) {
        Member member = memberService.login(request);
        sessionMemberResolver.login(servletRequest, member.getId());

        return ResponseEntity.ok(new MemberResponse(member));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        sessionMemberResolver.logout(servletRequest);
        return ResponseEntity.noContent().build();
    }
}
