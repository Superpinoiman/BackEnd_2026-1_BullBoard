package com.bullboard.dto;

import com.bullboard.domain.Member;

public class PublicProfileResponse {

    private final Long id;
    private final String nickname;
    private final String introduction;

    public PublicProfileResponse(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.introduction = member.getIntroduction();
    }

    public Long getId() { return id; }
    public String getNickname() { return nickname; }
    public String getIntroduction() { return introduction; }
}
