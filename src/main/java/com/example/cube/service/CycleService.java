package com.example.cube.service;

import com.example.cube.dto.response.CycleProcessDTO;
import com.example.cube.dto.response.CycleStatusDTO;
import com.example.cube.model.Cube;

import java.util.Map;
import java.util.UUID;

public interface CycleService {

    // Start the cube (transition from draft to active)
    Cube startCube(UUID cubeId, UUID userId);

    // Process current cycle (collect payments, select winner, payout)
    CycleProcessDTO processCycle(UUID cubeId);

    /**
     * Get current cycle status
     */
    CycleStatusDTO getCurrentCycleStatus(UUID cubeId);

    /**
     * Record a member's payment for a cycle
     */
    boolean recordMemberPayment(UUID cubeId, UUID userId, Integer cycleNumber);

    /**
     * Get payment status for all members in a specific cycle
     */
    Map<String, Object> getCyclePaymentStatus(UUID cubeId, Integer cycleNumber);
}