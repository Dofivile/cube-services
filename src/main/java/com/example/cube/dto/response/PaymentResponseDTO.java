package com.example.cube.dto.response;

/**
 * Response DTO for payment processing
 */
public class PaymentResponseDTO {

    private Boolean success;

    // Constructor
    public PaymentResponseDTO() {}

    public PaymentResponseDTO(Boolean success) {
        this.success = success;
    }

    // Getter and Setter
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
}