package com.example.cube.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class VerifyMicroDepositsRequest {

    @NotNull(message = "First amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount1;

    @NotNull(message = "Second amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal amount2;

    // Getters and Setters
    public BigDecimal getAmount1() { return amount1; }
    public void setAmount1(BigDecimal amount1) { this.amount1 = amount1; }

    public BigDecimal getAmount2() { return amount2; }
    public void setAmount2(BigDecimal amount2) { this.amount2 = amount2; }
}