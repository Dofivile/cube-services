package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a single transaction with enriched information
 */
public class TransactionDTO {

    private UUID paymentId;
    private UUID userId;
    private UUID cubeId;
    private String cubeName;
    private String type;           // "PAYMENT" or "PAYOUT"
    private String status;         // "PENDING", "COMPLETED", "FAILED"
    private BigDecimal amount;
    private String currency;
    private Integer cycleNumber;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String stripePaymentIntentId;
    private String stripeTransferId;
    private String failureReason;

    // Constructors
    public TransactionDTO() {}

    // Getters and Setters
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public String getCubeName() { return cubeName; }
    public void setCubeName(String cubeName) { this.cubeName = cubeName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(Integer cycleNumber) { this.cycleNumber = cycleNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public String getStripeTransferId() { return stripeTransferId; }
    public void setStripeTransferId(String stripeTransferId) {
        this.stripeTransferId = stripeTransferId;
    }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}