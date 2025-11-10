package com.example.cube.dto.response;

public class BankAccountStatusResponse {
    private boolean hasBankAccount;
    private String bankName;
    private String last4;
    private Boolean isDefault;
    private Boolean verified;
    private String paymentMethodId;

    public BankAccountStatusResponse(boolean hasBankAccount, String bankName, String last4,
                                     Boolean isDefault, Boolean verified, String paymentMethodId) {
        this.hasBankAccount = hasBankAccount;
        this.bankName = bankName;
        this.last4 = last4;
        this.isDefault = isDefault;
        this.verified = verified;
        this.paymentMethodId = paymentMethodId;
    }

    // Getters and Setters
    public boolean isHasBankAccount() { return hasBankAccount; }
    public void setHasBankAccount(boolean hasBankAccount) { this.hasBankAccount = hasBankAccount; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
}
