package com.bullboard.dto;

public class NicknameAvailabilityResponse {

    private final boolean available;

    public NicknameAvailabilityResponse(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() { return available; }
}
