package com.example.cube.dto.response;

public class PlaidLinkResponse {
    private String message;
    private String fundingSourceId;
    private String fundingSourceStatus;
    private String bankName;
    private String accountType;
    private String accountMask; // Last 4 digits

    public PlaidLinkResponse() {}

    public PlaidLinkResponse(String message, String fundingSourceId) {
        this.message = message;
        this.fundingSourceId = fundingSourceId;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getFundingSourceId() { return fundingSourceId; }
    public void setFundingSourceId(String fundingSourceId) { this.fundingSourceId = fundingSourceId; }

    public String getFundingSourceStatus() { return fundingSourceStatus; }
    public void setFundingSourceStatus(String fundingSourceStatus) {
        this.fundingSourceStatus = fundingSourceStatus;
    }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getAccountMask() { return accountMask; }
    public void setAccountMask(String accountMask) { this.accountMask = accountMask; }
}