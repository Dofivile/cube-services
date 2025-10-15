package com.example.cube.dto.response;

import java.util.List;
import java.util.UUID;

public class GetUserCubesResponse {
    private UUID userId;
    private List<UUID> cubeIds;

    public GetUserCubesResponse() {}

    public GetUserCubesResponse(UUID userId, List<UUID> cubeIds) {
        this.userId = userId;
        this.cubeIds = cubeIds;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<UUID> getCubeIds() {
        return cubeIds;
    }

    public void setCubeIds(List<UUID> cubeIds) {
        this.cubeIds = cubeIds;
    }
}