package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for processing a payment
 */
public class PaymentRequestDTO {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID cubeId;

    @NotNull
    private UUID memberId;

    @NotNull
    private Integer cycleNumber;

    @NotNull
    @Positive
    private BigDecimal amount;

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public Integer getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(Integer cycleNumber) { this.cycleNumber = cycleNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}