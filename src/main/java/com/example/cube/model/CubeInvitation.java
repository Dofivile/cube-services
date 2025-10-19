package com.example.cube.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cube_invitations", schema = "public")
public class CubeInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "invitation_id", columnDefinition = "UUID")
    private UUID invitationId;

    @Column(name = "cube_id", nullable = false)
    private UUID cubeId;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Column(name = "invitee_id")
    private UUID inviteeId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "invite_token", nullable = false, unique = true)
    private String inviteToken;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "status", nullable = false)
    private String status = "pending";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Getters and Setters only - no business logic
    public UUID getInvitationId() { return invitationId; }
    public void setInvitationId(UUID invitationId) { this.invitationId = invitationId; }

    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public UUID getInvitedBy() { return invitedBy; }
    public void setInvitedBy(UUID invitedBy) { this.invitedBy = invitedBy; }

    public UUID getInviteeId() { return inviteeId; }
    public void setInviteeId(UUID inviteeId) { this.inviteeId = inviteeId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInviteToken() { return inviteToken; }
    public void setInviteToken(String inviteToken) { this.inviteToken = inviteToken; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}