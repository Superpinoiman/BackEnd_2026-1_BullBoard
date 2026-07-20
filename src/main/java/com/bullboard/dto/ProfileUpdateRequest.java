package com.bullboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank
    @Size(max = 50)
    private String nickname;

    @Size(max = 200)
    private String introduction;

    public String getNickname() { return nickname; }
    public String getIntroduction() { return introduction; }
}
