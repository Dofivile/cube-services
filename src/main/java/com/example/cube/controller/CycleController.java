package com.example.cube.controller;

import com.example.cube.dto.response.CycleProcessDTO;
import com.example.cube.dto.response.CycleStatusDTO;
import com.example.cube.model.Cube;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.CycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cubes")
public class CycleController {

    private final CycleService cycleService;
    private final AuthenticationService authenticationService;

    @Autowired
    public CycleController(CycleService cycleService,
                           AuthenticationService authenticationService) {
        this.cycleService = cycleService;
        this.authenticationService = authenticationService;
    }

    /**
     * POST /api/cubes/{cubeId}/start
     * Start a cube (transition from draft to pending_payment)
     */
    @PostMapping("/{cubeId}/start")
    public ResponseEntity<Cube> startCube(
            @PathVariable UUID cubeId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        Cube cube = cycleService.startCube(cubeId, userId);
        return ResponseEntity.ok(cube);
    }

    /**
     * POST /api/cubes/{cubeId}/cycles/{cycleNumber}/payments
     * Record a member's payment for a cycle
     */
    @PostMapping("/{cubeId}/cycles/{cycleNumber}/payments")
    public ResponseEntity<Map<String, Object>> recordPayment(
            @PathVariable UUID cubeId,
            @PathVariable Integer cycleNumber,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        boolean allPaid = cycleService.recordMemberPayment(cubeId, userId, cycleNumber);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "allMembersPaid", allPaid,
                "message", allPaid ? "All members have paid! Cube is now active." : "Payment recorded. Waiting for other members."
        ));
    }

    /**
     * GET /api/cubes/{cubeId}/cycles/{cycleNumber}/payment-status
     * Check payment status for all members for a specific cycle
     */
    @GetMapping("/{cubeId}/cycles/{cycleNumber}/payment-status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(
            @PathVariable UUID cubeId,
            @PathVariable Integer cycleNumber,
            @RequestHeader("Authorization") String authHeader) {

        authenticationService.validateAndExtractUserId(authHeader);

        Map<String, Object> status = cycleService.getCyclePaymentStatus(cubeId, cycleNumber);
        return ResponseEntity.ok(status);
    }

    /**
     * POST /api/cubes/{cubeId}/cycles/process
     * Process current cycle (collect payments, select winner, payout)
     */
    @PostMapping("/{cubeId}/cycles/process")
    public ResponseEntity<CycleProcessDTO> processCycle(
            @PathVariable UUID cubeId,
            @RequestHeader("Authorization") String authHeader) {

        authenticationService.validateAndExtractUserId(authHeader);
        CycleProcessDTO result = cycleService.processCycle(cubeId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/cubes/{cubeId}/cycles/status
     * Get current cycle status
     */
    @GetMapping("/{cubeId}/cycles/status")
    public ResponseEntity<CycleStatusDTO> getCycleStatus(
            @PathVariable UUID cubeId,
            @RequestHeader("Authorization") String authHeader) {

        authenticationService.validateAndExtractUserId(authHeader);
        CycleStatusDTO status = cycleService.getCurrentCycleStatus(cubeId);
        return ResponseEntity.ok(status);
    }
}