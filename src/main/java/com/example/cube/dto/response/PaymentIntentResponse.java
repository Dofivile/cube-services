package com.example.cube.dto.response;

public class PaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String customerId;
    private String ephemeralKeySecret;

    public PaymentIntentResponse(String clientSecret, String paymentIntentId, String customerId, String ephemeralKeySecret) {
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.customerId = customerId;
        this.ephemeralKeySecret = ephemeralKeySecret;
    }

    // Getters and Setters
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(String paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getEphemeralKeySecret() { return ephemeralKeySecret; }
    public void setEphemeralKeySecret(String ephemeralKeySecret) { this.ephemeralKeySecret = ephemeralKeySecret; }
}
