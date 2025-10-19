package com.example.cube.dto.response;

public class PaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String customerId;

    public PaymentIntentResponse(String clientSecret, String paymentIntentId, String customerId) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.customerId = customerId;
    }

    // Getters and Setters
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}