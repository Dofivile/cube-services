package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public class CycleProcessDTO {

    private Integer cycle;
    private UUID winnerUserId;
    private BigDecimal payoutAmount;
    private Integer remainingMembers;
    private Boolean isComplete;
    private BigDecimal bankBalance;

    // Constructors
    public CycleProcessDTO() {}

    // Getters and Setters
    public Integer getCycle() {
        return cycle;
    }

    public void setCycle(Integer cycle) {
        this.cycle = cycle;
    }

    public UUID getWinnerUserId() {
        return winnerUserId;
    }

    public void setWinnerUserId(UUID winnerUserId) {
        this.winnerUserId = winnerUserId;
    }

    public BigDecimal getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(BigDecimal payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public Integer getRemainingMembers() {
        return remainingMembers;
    }

    public void setRemainingMembers(Integer remainingMembers) {
        this.remainingMembers = remainingMembers;
    }

    public Boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public BigDecimal getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(BigDecimal bankBalance) {
        this.bankBalance = bankBalance;
    }
}