package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class GetUserCubesRequest {
    @NotNull
    private UUID userId;

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}
