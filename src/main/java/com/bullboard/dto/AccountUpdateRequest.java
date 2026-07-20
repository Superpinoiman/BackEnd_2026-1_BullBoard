package com.bullboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountUpdateRequest {

    @Email
    @NotBlank
    private String email;

    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
    )
    private String password;

    private String passwordConfirm;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPasswordConfirm() { return passwordConfirm; }
}
