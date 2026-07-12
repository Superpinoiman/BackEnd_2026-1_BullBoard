package com.bullboard.dto;

import jakarta.validation.constraints.NotBlank;

public class BoardRequest {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }
}
