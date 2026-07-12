package com.bullboard.dto;

import com.bullboard.domain.Member;

public class MemberResponse {

    private final Long id;
    private final String email;
    private final String nickname;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getNickname() { return nickname; }
}
