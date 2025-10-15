package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class GetUserCubesRequest {
    @NotNull
    private UUID user_id;

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public UUID getUser_id() {
        return user_id;
    }
}
