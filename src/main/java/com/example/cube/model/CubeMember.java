package com.example.cube.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cube_members", schema = "public")
public class CubeMember {

    // ========== Primary Key ==========
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id", columnDefinition = "UUID")
    private UUID memberId;

    // ========== Foreign Keys (As UUIDs) ==========
    @Column(name = "cube_id", nullable = false)
    private UUID cubeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable= false)
    private Integer roleId;

    // ========== Timestamps ==========
    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    // ========== Payout Related Fields ==========
    @Column(name = "payout_position")
    private Integer payoutPosition;

    @Column(name = "has_received_payout")
    private Boolean hasReceivedPayout = false;

    @Column(name = "payout_date")
    private LocalDateTime payoutDate;


    // ========== Getters and Setters ==========
    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public Integer getPayoutPosition() { return payoutPosition; }
    public void setPayoutPosition(Integer payoutPosition) { this.payoutPosition = payoutPosition; }

    public Boolean getHasReceivedPayout() { return hasReceivedPayout; }
    public void setHasReceivedPayout(Boolean hasReceivedPayout) { this.hasReceivedPayout = hasReceivedPayout; }

    public LocalDateTime getPayoutDate() { return payoutDate; }
    public void setPayoutDate(LocalDateTime payoutDate) { this.payoutDate = payoutDate; }
}