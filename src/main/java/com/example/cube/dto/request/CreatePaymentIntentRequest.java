package com.example.cube.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public class CreatePaymentIntentRequest {
    private UUID cubeId;
    private UUID memberId;
    private Integer cycleNumber;

    // Getters and Setters
    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public UUID getMemberId() { return memberId; }
    public void setMemberId(UUID memberId) { this.memberId = memberId; }

    public Integer getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(Integer cycleNumber) { this.cycleNumber = cycleNumber; }
}