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
        private String roleName;  // "admin" or "member"
        private String firstName;
        private String lastName;
        private LocalDateTime joinedAt;
        private Boolean hasReceivedPayout;
        private Integer payoutPosition;
        
        // ✅ ADD: Payment status fields
        private Integer statusId;  // 1 = awaiting payment, 2 = paid
        private String paymentStatus;  // "awaiting payment" or "paid"

        public MemberInfo() {}

        // Existing Getters and Setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public UUID getMemberId() { return memberId; }
        public void setMemberId(UUID memberId) { this.memberId = memberId; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

        public Boolean getHasReceivedPayout() { return hasReceivedPayout; }
        public void setHasReceivedPayout(Boolean hasReceivedPayout) { this.hasReceivedPayout = hasReceivedPayout; }

        public Integer getPayoutPosition() { return payoutPosition; }
        public void setPayoutPosition(Integer payoutPosition) { this.payoutPosition = payoutPosition; }

        // ✅ ADD: New Getters and Setters
        public Integer getStatusId() { return statusId; }
        public void setStatusId(Integer statusId) { this.statusId = statusId; }

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }

    // Getters and Setters
    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public Integer getTotalMembers() { return totalMembers; }
    public void setTotalMembers(Integer totalMembers) { this.totalMembers = totalMembers; }

    public List<MemberInfo> getMembers() { return members; }
    public void setMembers(List<MemberInfo> members) { this.members = members; }
}
