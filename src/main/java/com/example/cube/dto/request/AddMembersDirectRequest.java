package com.example.cube.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class AddMembersDirectRequest {

    @NotNull(message = "Cube ID is required")
    private UUID cubeId;

    @NotEmpty(message = "Must add at least one member")
    private List<UUID> userIds;  // Direct user IDs, not emails!

    // roleId field REMOVED - backend will enforce roleId = 2 for added members

    // Getters and Setters
    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public List<UUID> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<UUID> userIds) {
        this.userIds = userIds;
    }
}