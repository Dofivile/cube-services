package com.example.cube.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "CubeMemebers")
public class CubeMember {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "cube_id", nullable = false)
    private UUID cubeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "payout_order", nullable = false)
    private int payoutOrder;

    @Column(name = "has_received_payout")
    private boolean hasReceivedPayout = false;

    @Column(name = "payout_received_at")
    private Timestamp payoutReceivedAt;

    @Column(name = "total_contributed")
    private BigDecimal totalContributed = BigDecimal.ZERO;

    @Column(name = "last_contribution_date")
    private Timestamp lastContributionDate;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "contribution_status")
    private String contributionStatus = "current";

    @Column(name = "joined_at")
    private Timestamp joinedAt = new Timestamp(System.currentTimeMillis());

    @Column(name = "invited_by_user_id")
    private UUID invitedByUserId;

    // Getters and Setters
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public int getPayoutOrder() { return payoutOrder; }
    public void setPayoutOrder(int payoutOrder) { this.payoutOrder = payoutOrder; }

    public boolean isHasReceivedPayout() { return hasReceivedPayout; }
    public void setHasReceivedPayout(boolean hasReceivedPayout) { this.hasReceivedPayout = hasReceivedPayout; }

    public Timestamp getPayoutReceivedAt() { return payoutReceivedAt; }
    public void setPayoutReceivedAt(Timestamp payoutReceivedAt) { this.payoutReceivedAt = payoutReceivedAt; }

    public BigDecimal getTotalContributed() { return totalContributed; }
    public void setTotalContributed(BigDecimal totalContributed) { this.totalContributed = totalContributed; }

    public Timestamp getLastContributionDate() { return lastContributionDate; }
    public void setLastContributionDate(Timestamp lastContributionDate) { this.lastContributionDate = lastContributionDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getContributionStatus() { return contributionStatus; }
    public void setContributionStatus(String contributionStatus) { this.contributionStatus = contributionStatus; }

    public Timestamp getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }

    public UUID getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(UUID invitedByUserId) { this.invitedByUserId = invitedByUserId; }
}
