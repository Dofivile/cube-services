package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CycleStatusDTO {

    private UUID cubeId;
    private Integer currentCycle;
    private Integer totalCycles;
    private BigDecimal totalToBeCollected;
    private BigDecimal totalAmountCollected;
    private BigDecimal progressPercentage;
    private Instant nextPayoutDate;
    private Integer remainingMembers;
    private Boolean isComplete;
    private List<MemberPayoutStatus> members;

    // Constructors
    public CycleStatusDTO() {}

    // Getters and Setters
    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public Integer getCurrentCycle() {
        return currentCycle;
    }

    public void setCurrentCycle(Integer currentCycle) {
        this.currentCycle = currentCycle;
    }

    public Integer getTotalCycles() {
        return totalCycles;
    }

    public void setTotalCycles(Integer totalCycles) {
        this.totalCycles = totalCycles;
    }

    public BigDecimal getTotalToBeCollected() {
        return totalToBeCollected;
    }

    public void setTotalToBeCollected(BigDecimal totalToBeCollected) {
        this.totalToBeCollected = totalToBeCollected;
    }

    public BigDecimal getTotalAmountCollected() {
        return totalAmountCollected;
    }

    public void setTotalAmountCollected(BigDecimal totalAmountCollected) {
        this.totalAmountCollected = totalAmountCollected;
    }

    public BigDecimal getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(BigDecimal progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Instant getNextPayoutDate() {
        return nextPayoutDate;
    }

    public void setNextPayoutDate(Instant nextPayoutDate) {
        this.nextPayoutDate = nextPayoutDate;
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

    public List<MemberPayoutStatus> getMembers() {
        return members;
    }

    public void setMembers(List<MemberPayoutStatus> members) {
        this.members = members;
    }
}