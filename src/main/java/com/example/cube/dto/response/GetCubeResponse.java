package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class GetCubeResponse {

    private UUID cubeId;
    private String name;
    private UUID userId;
    private String description;
    private BigDecimal amountPerCycle;
    private Instant nextPayoutDate;
    private Integer currentCycle;
    private String currency;
    private Integer numberOfMembers;
    private Instant startDate;
    private Instant endDate;
    private String contributionFrequency;  // "daily", "weekly", or "monthly"
    private Integer contributionFrequencyDays;  // 1, 7, or 30
    private Integer statusId;  // 1=draft, 2=active, 3=completed, 4=cancelled
    private Integer goalTypeId;
    private String goalTypeName;
    private String invitationCode;
    private BigDecimal totalToBeCollected;
    private BigDecimal totalAmountCollected;

    public GetCubeResponse() {}

    public UUID getCubeId() {
        return cubeId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContributionFrequency() {
        return contributionFrequency;
    }

    public Integer getContributionFrequencyDays() {
        return contributionFrequencyDays;
    }

    public void setContributionFrequency(String contributionFrequency) {
        this.contributionFrequency = contributionFrequency;
    }

    public void setContributionFrequencyDays(Integer contributionFrequencyDays) {
        this.contributionFrequencyDays = contributionFrequencyDays;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public BigDecimal getAmountPerCycle() {
        return amountPerCycle;
    }

    public void setAmountPerCycle(BigDecimal amountPerCycle) {
        this.amountPerCycle = amountPerCycle;
    }

    public Instant getNextPayoutDate() {
        return nextPayoutDate;
    }

    public void setNextPayoutDate(Instant nextPayoutDate) {
        this.nextPayoutDate = nextPayoutDate;
    }

    public Integer getCurrentCycle() {
        return currentCycle;
    }

    public void setCurrentCycle(Integer currentCycle) {
        this.currentCycle = currentCycle;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getNumberOfMembers() {
        return numberOfMembers;
    }

    public void setNumberOfMembers(Integer numberOfMembers) {
        this.numberOfMembers = numberOfMembers;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Integer getGoalTypeId() {
        return goalTypeId;
    }

    public void setGoalTypeId(Integer goalTypeId) {
        this.goalTypeId = goalTypeId;
    }

    public String getGoalTypeName() {
        return goalTypeName;
    }

    public void setGoalTypeName(String goalTypeName) {
        this.goalTypeName = goalTypeName;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
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
}
