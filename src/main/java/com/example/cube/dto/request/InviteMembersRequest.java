package com.example.cube.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class InviteMembersRequest {

    @NotNull(message = "Cube ID is required")
    private UUID cubeId;  // ← ADD THIS

    @NotEmpty(message = "Must invite at least one member")
    private List<UUID> userIds;

    @NotNull(message = "Role is required")
    private Integer roleId;

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

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
}