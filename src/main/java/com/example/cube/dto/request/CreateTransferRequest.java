package com.example.cube.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateTransferRequest {

    @NotNull(message = "Cube ID is required")
    private UUID cubeId;

    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Cycle number is required")
    private Integer cycleNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount;

    // Optional: for custom transfers
    private String sourceFundingSourceId;
    private String destinationFundingSourceId;

    // Getters and Setters
    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public Integer getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(Integer cycleNumber) { this.cycleNumber = cycleNumber; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getSourceFundingSourceId() { return sourceFundingSourceId; }
    public void setSourceFundingSourceId(String sourceFundingSourceId) {
        this.sourceFundingSourceId = sourceFundingSourceId;
    }

    public String getDestinationFundingSourceId() { return destinationFundingSourceId; }
    public void setDestinationFundingSourceId(String destinationFundingSourceId) {
        this.destinationFundingSourceId = destinationFundingSourceId;
    }
}