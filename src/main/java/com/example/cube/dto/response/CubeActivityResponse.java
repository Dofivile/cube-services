package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CubeActivityResponse {
    
    private String activityType;      // "PAYMENT", "WINNER", "MEMBER_JOIN"
    private String activityText;      // "Sarah M. contributed to pool"
    private String userName;          // "Sarah M."
    private UUID userId;
    private LocalDateTime timestamp;  // For sorting
    private BigDecimal amount;        // For payments/winners (optional)
    private Integer cycleNumber;      // For payments/winners (optional)
    private String colorCode;         // "green", "yellow", "blue"

    // Constructors
    public CubeActivityResponse() {}

    // Getters and Setters
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getActivityText() {
        return activityText;
    }

    public void setActivityText(String activityText) {
        this.activityText = activityText;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(Integer cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }
}

