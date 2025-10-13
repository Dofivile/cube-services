package com.example.cube.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class MemberPayoutStatus {

    private UUID userId;
    private Boolean hasReceived;
    private Integer payoutCycle;
    private LocalDateTime payoutDate;

    // Constructors
    public MemberPayoutStatus() {}

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Boolean getHasReceived() {
        return hasReceived;
    }

    public void setHasReceived(Boolean hasReceived) {
        this.hasReceived = hasReceived;
    }

    public Integer getPayoutCycle() {
        return payoutCycle;
    }

    public void setPayoutCycle(Integer payoutCycle) {
        this.payoutCycle = payoutCycle;
    }

    public LocalDateTime getPayoutDate() {
        return payoutDate;
    }

    public void setPayoutDate(LocalDateTime payoutDate) {
        this.payoutDate = payoutDate;
    }
}