package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class VerifyAdminRequest {

    @NotNull
    private UUID cubeId;

    public VerifyAdminRequest() {}

    public VerifyAdminRequest(UUID cubeId) {
        this.cubeId = cubeId;
    }

    // Getters and Setters
    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }
}