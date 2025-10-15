package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class StartCubeRequest {
    @NotNull
    private UUID user_id;

    @NotNull
    private UUID cubeId;

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public UUID getUser_id() {
        return user_id;
    }
}
