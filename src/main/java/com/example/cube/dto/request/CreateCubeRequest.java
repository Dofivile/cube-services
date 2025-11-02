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
public class CreateCubeRequest {

    @NotBlank
    private String name;
    private String description;
    @NotNull
    @Positive
    private BigDecimal amountPerCycle;
    @NotNull
    private Integer durationId;
    @NotNull
    @Positive
    private Integer numberofmembers;
    @NotBlank
    private String currency;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmountPerCycle() { return amountPerCycle; }
    public void setAmountPerCycle(BigDecimal amountPerCycle) { this.amountPerCycle = amountPerCycle; }

    public Integer getDurationId() {return durationId;}
    public void setDurationId(Integer durationId) {this.durationId = durationId;}

    public Integer getNumberofmembers() { return numberofmembers; }
    public void setNumberofmembers(Integer numberofmembers) { this.numberofmembers = numberofmembers; }


    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
