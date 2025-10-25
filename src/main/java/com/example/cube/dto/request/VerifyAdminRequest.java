package com.example.cube.dto.request;

import java.util.UUID;

public class VerifyAdminRequest {

    private UUID userId;
    private UUID cubeId;

    public VerifyAdminRequest() {}

    public VerifyAdminRequest(UUID userId, UUID cubeId) {
        this.userId = userId;
        this.cubeId = cubeId;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }
}