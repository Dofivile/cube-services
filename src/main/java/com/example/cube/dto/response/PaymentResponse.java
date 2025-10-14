package com.example.cube.dto.response;

/**
 * Response DTO for payment processing
 */
public class PaymentResponse {

    private Boolean success;

    // Constructor
    public PaymentResponse() {}

    public PaymentResponse(Boolean success) {
        this.success = success;
    }

    // Getter and Setter
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
}