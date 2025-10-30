package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class StartCubeRequest {
    @NotNull
    private UUID memberId;

    @NotNull
    private UUID cubeId;

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }
}
