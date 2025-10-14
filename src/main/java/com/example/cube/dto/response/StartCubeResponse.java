package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for starting a cube
 */
public class StartCubeResponse {

    private UUID cubeId;
    private Integer statusId;
    private Integer currentCycle;
    private Instant startDate;
    private Instant endDate;
    private BigDecimal totalToBeCollected;

    // Constructors
    public StartCubeResponse() {}

    public StartCubeResponse(UUID cubeId, Integer statusId, Integer currentCycle, Instant startDate, Instant endDate, BigDecimal totalToBeCollected) {
        this.cubeId = cubeId;
        this.statusId = statusId;
        this.currentCycle = currentCycle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalToBeCollected = totalToBeCollected;
    }

    // Getters and Setters
    public UUID getCubeId() { return cubeId; }
    public void setCubeId(UUID cubeId) { this.cubeId = cubeId; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public Integer getCurrentCycle() { return currentCycle; }
    public void setCurrentCycle(Integer currentCycle) { this.currentCycle = currentCycle; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public BigDecimal getTotalToBeCollected() { return totalToBeCollected; }
    public void setTotalToBeCollected(BigDecimal totalToBeCollected) { this.totalToBeCollected = totalToBeCollected; }
}