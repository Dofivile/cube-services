package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for returning Cube details to the frontend.
 */
public class CubeResponseDTO {

    private UUID cubeId;
    private String name;
    private String description;
    private UUID user_id;
    private BigDecimal amountPerCycle;
    private Integer numberofmembers;
    private String currency;
    private Instant startDate;
    private Instant endDate;
    private Instant nextPayoutDate;
    private Integer durationId;
    private String durationName;
    private Integer durationDays;
    private Instant createdAt;

    // Getters and Setters
    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getUser_id() { return user_id; }
    public void setUser_id(UUID user_id) { this.user_id = user_id; }

    public BigDecimal getAmountPerCycle() { return amountPerCycle; }
    public void setAmountPerCycle(BigDecimal amountPerCycle) { this.amountPerCycle = amountPerCycle; }

    public Integer getNumberofmembers() { return numberofmembers; }
    public void setNumberofmembers(Integer numberofmembers) { this.numberofmembers = numberofmembers; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public Instant getNextPayoutDate() { return nextPayoutDate; }
    public void setNextPayoutDate(Instant nextPayoutDate) { this.nextPayoutDate = nextPayoutDate; }

    public Integer getDurationId() { return durationId; }
    public void setDurationId(Integer durationId) { this.durationId = durationId; }

    public String getDurationName() { return durationName; }
    public void setDurationName(String durationName) { this.durationName = durationName; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
