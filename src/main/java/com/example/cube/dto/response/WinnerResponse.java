package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WinnerResponse {
    private UUID winnerId;
    private UUID userId;
    private String userName;
    private String userInitial;
    private Integer cycleNumber;
    private BigDecimal payoutAmount;
    private LocalDateTime selectedAt;
    private Boolean payoutSent;

    // Constructors
    public WinnerResponse() {}

    public WinnerResponse(UUID winnerId, UUID userId, String userName, String userInitial,
                         Integer cycleNumber, BigDecimal payoutAmount, 
                         LocalDateTime selectedAt, Boolean payoutSent) {
        this.winnerId = winnerId;
        this.userId = userId;
        this.userName = userName;
        this.userInitial = userInitial;
        this.cycleNumber = cycleNumber;
        this.payoutAmount = payoutAmount;
        this.selectedAt = selectedAt;
        this.payoutSent = payoutSent;
    }

    // Getters and Setters
    public UUID getWinnerId() { 
        return winnerId; 
    }
    
    public void setWinnerId(UUID winnerId) { 
        this.winnerId = winnerId; 
    }

    public UUID getUserId() { 
        return userId; 
    }
    
    public void setUserId(UUID userId) { 
        this.userId = userId; 
    }

    public String getUserName() { 
        return userName; 
    }
    
    public void setUserName(String userName) { 
        this.userName = userName; 
    }

    public String getUserInitial() { 
        return userInitial; 
    }
    
    public void setUserInitial(String userInitial) { 
        this.userInitial = userInitial; 
    }

    public Integer getCycleNumber() { 
        return cycleNumber; 
    }
    
    public void setCycleNumber(Integer cycleNumber) { 
        this.cycleNumber = cycleNumber; 
    }

    public BigDecimal getPayoutAmount() { 
        return payoutAmount; 
    }
    
    public void setPayoutAmount(BigDecimal payoutAmount) { 
        this.payoutAmount = payoutAmount; 
    }

    public LocalDateTime getSelectedAt() { 
        return selectedAt; 
    }
    
    public void setSelectedAt(LocalDateTime selectedAt) { 
        this.selectedAt = selectedAt; 
    }

    public Boolean getPayoutSent() { 
        return payoutSent; 
    }
    
    public void setPayoutSent(Boolean payoutSent) { 
        this.payoutSent = payoutSent; 
    }
}

