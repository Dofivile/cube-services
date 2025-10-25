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
}