package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for starting a cube
 */
public class StartCubeRequest {

    @NotNull(message = "Cube ID is required")
    private UUID cubeId;

    // Constructors
    public StartCubeRequest() {}

    public StartCubeRequest(UUID cubeId) {
        this.cubeId = cubeId;
    }

    // Getter and Setter
    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }
}