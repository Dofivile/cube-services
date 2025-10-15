package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class GetCubeRequest {

    @NotNull
    private UUID cubeId;

    public GetCubeRequest() {}

    public GetCubeRequest(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }
}