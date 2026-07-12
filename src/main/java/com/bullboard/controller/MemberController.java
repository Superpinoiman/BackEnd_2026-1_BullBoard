package com.bullboard.controller;

import com.bullboard.auth.SessionMemberResolver;
import com.bullboard.dto.MemberRequest;
import com.bullboard.dto.MemberResponse;
import com.bullboard.service.MemberService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final SessionMemberResolver sessionMemberResolver;

    public MemberController(MemberService memberService,
                            SessionMemberResolver sessionMemberResolver) {
        this.memberService = memberService;
        this.sessionMemberResolver = sessionMemberResolver;
    }

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(
            @Valid @RequestBody MemberRequest request,
            HttpServletRequest servletRequest) {
        var member = memberService.createMember(request);

        sessionMemberResolver.login(servletRequest, member.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MemberResponse(member));
    }
}
