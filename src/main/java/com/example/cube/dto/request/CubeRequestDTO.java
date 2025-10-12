package com.example.cube.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for creating a Cube.
 * Only includes fields the frontend should send.
 * The database will handle IDs and timestamps.
 */
public class CubeRequestDTO {

    @NotBlank
    private String name;
    private String description;
    @NotNull
    private UUID user_id;
    @NotNull
    @Positive
    private BigDecimal amountPerCycle;
    @NotNull
    private Integer durationId;
    @NotNull
    @Positive
    private Integer numberofmembers;
    @NotNull
    private Instant startDate;

    @NotBlank
    private String currency;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getUser_id() { return user_id; }
    public void setUser_id(UUID user_id) { this.user_id = user_id; }

    public BigDecimal getAmountPerCycle() { return amountPerCycle; }
    public void setAmountPerCycle(BigDecimal amountPerCycle) { this.amountPerCycle = amountPerCycle; }

    public Integer getDurationId() {return durationId;}
    public void setDurationId(Integer durationId) {this.durationId = durationId;}

    public Integer getNumberofmembers() { return numberofmembers; }
    public void setNumberofmembers(Integer numberofmembers) { this.numberofmembers = numberofmembers; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
