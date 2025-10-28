package com.example.cube.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class FundingSourceResponse {
    private String fundingSourceId;
    private String fundingSourceUrl;
    private String status; // "unverified", "verified", "removed"
    private String type; // "bank"
    private String bankAccountType; // "checking", "savings", etc.
    private String name;
    private String bankName;
    private LocalDateTime created;
    private String message;
    private List<String> channels; // Available channels for microdeposits

    public FundingSourceResponse() {}

    public FundingSourceResponse(String fundingSourceId, String status, String fundingSourceUrl) {
        this.fundingSourceId = fundingSourceId;
        this.status = status;
        this.fundingSourceUrl = fundingSourceUrl;
    }

    // Getters and Setters
    public String getFundingSourceId() { return fundingSourceId; }
    public void setFundingSourceId(String fundingSourceId) { this.fundingSourceId = fundingSourceId; }

    public String getFundingSourceUrl() { return fundingSourceUrl; }
    public void setFundingSourceUrl(String fundingSourceUrl) { this.fundingSourceUrl = fundingSourceUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBankAccountType() { return bankAccountType; }
    public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }
}