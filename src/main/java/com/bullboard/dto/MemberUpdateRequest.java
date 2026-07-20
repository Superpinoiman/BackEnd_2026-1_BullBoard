package com.bullboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberUpdateRequest {

    @NotBlank
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @Size(max = 200)
    private String introduction;

    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
    )
    private String password;

    private String passwordConfirm;

    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public String getIntroduction() { return introduction; }
    public String getPassword() { return password; }
    public String getPasswordConfirm() { return passwordConfirm; }
}
