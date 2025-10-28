package com.example.cube.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateFundingSourceRequest {

    @NotBlank(message = "Routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "Routing number must be exactly 9 digits")
    private String routingNumber;

    @NotBlank(message = "Account number is required")
    @Size(min = 4, max = 17, message = "Account number must be between 4 and 17 digits")
    @Pattern(regexp = "^\\d+$", message = "Account number must contain only digits")
    private String accountNumber;

    @NotBlank(message = "Bank account type is required")
    @Pattern(regexp = "^(checking|savings|loan|general-ledger)$",
            message = "Bank account type must be: checking, savings, loan, or general-ledger")
    private String bankAccountType;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be ≤ 50 characters")
    private String name;

    // Optional: channels for micro-deposit verification
    private String channels; // "email" or "sms" or "email,sms"

    // Getters and Setters
    public String getRoutingNumber() { return routingNumber; }
    public void setRoutingNumber(String routingNumber) { this.routingNumber = routingNumber; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankAccountType() { return bankAccountType; }
    public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getChannels() { return channels; }
    public void setChannels(String channels) { this.channels = channels; }
}