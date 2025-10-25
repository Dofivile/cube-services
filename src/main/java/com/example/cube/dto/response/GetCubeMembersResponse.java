package com.example.cube.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GetCubeMembersResponse {

    private UUID cubeId;
    private Integer totalMembers;
    private List<MemberInfo> members;

    public GetCubeMembersResponse() {}

    public GetCubeMembersResponse(UUID cubeId, Integer totalMembers, List<MemberInfo> members) {
        this.cubeId = cubeId;
        this.totalMembers = totalMembers;
        this.members = members;
    }

    // Inner class for member details
    public static class MemberInfo {
        private UUID userId;
        private UUID memberId;
        private Integer roleId;
        private String roleName;  // "admin" or "member"
        private LocalDateTime joinedAt;
        private Boolean hasReceivedPayout;
        private Integer payoutPosition;

        public MemberInfo() {}

        // Getters and Setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public UUID getMemberId() { return memberId; }
        public void setMemberId(UUID memberId) { this.memberId = memberId; }

        public Integer getRoleId() { return roleId; }
        public void setRoleId(Integer roleId) { this.roleId = roleId; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

        public Boolean getHasReceivedPayout() { return hasReceivedPayout; }
        public void setHasReceivedPayout(Boolean hasReceivedPayout) { this.hasReceivedPayout = hasReceivedPayout; }

        public Integer getPayoutPosition() { return payoutPosition; }
        public void setPayoutPosition(Integer payoutPosition) { this.payoutPosition = payoutPosition; }
    }

    // Getters and Setters
    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public Integer getTotalMembers() { return totalMembers; }
    public void setTotalMembers(Integer totalMembers) { this.totalMembers = totalMembers; }

    public List<MemberInfo> getMembers() { return members; }
    public void setMembers(List<MemberInfo> members) { this.members = members; }
}