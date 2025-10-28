package com.example.cube.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PlaidExchangeRequest {

    @NotBlank(message = "Public token is required")
    private String publicToken;

    @NotBlank(message = "Account ID is required")
    private String accountId;

    private String institutionName; // Optional: bank name from Plaid

    // Getters and Setters
    public String getPublicToken() { return publicToken; }
    public void setPublicToken(String publicToken) { this.publicToken = publicToken; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
}