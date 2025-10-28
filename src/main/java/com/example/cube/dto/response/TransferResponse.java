package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class TransferResponse {
    private String transferId;
    private String transferUrl;
    private String status; // "pending", "processed", "failed", "cancelled"
    private BigDecimal amount;
    private String sourceFundingSourceId;
    private String destinationFundingSourceId;
    private LocalDateTime created;
    private String correlationId;
    private Map<String, String> metadata;
    private String message;

    // Failure details
    private String failureCode;
    private String failureDescription;

    public TransferResponse() {}

    public TransferResponse(String transferId, String status, BigDecimal amount) {
        this.transferId = transferId;
        this.status = status;
        this.amount = amount;
    }

    // Getters and Setters
    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public String getTransferUrl() { return transferUrl; }
    public void setTransferUrl(String transferUrl) { this.transferUrl = transferUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }

    public String getFailureDescription() { return failureDescription; }
    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription;
    }
}