package com.example.cube.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cycle_winners", schema = "public")
public class CycleWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "winner_id", columnDefinition = "uuid")
    private UUID winnerId;

    @Column(name = "cube_id", nullable = false)
    private UUID cubeId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cycle_number", nullable = false)
    private Integer cycleNumber;

    @Column(name = "payout_amount", nullable = false)
    private BigDecimal payoutAmount;

    @Column(name = "selected_at", nullable = false)
    private LocalDateTime selectedAt = LocalDateTime.now();

    @Column(name = "payout_sent")
    private Boolean payoutSent = false;

    // Getters and Setters
    public UUID getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(UUID winnerId) {
        this.winnerId = winnerId;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(Integer cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(BigDecimal payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public LocalDateTime getSelectedAt() {
        return selectedAt;
    }

    public void setSelectedAt(LocalDateTime selectedAt) {
        this.selectedAt = selectedAt;
    }

    public Boolean getPayoutSent() {
        return payoutSent;
    }

    public void setPayoutSent(Boolean payoutSent) {
        this.payoutSent = payoutSent;
    }
}