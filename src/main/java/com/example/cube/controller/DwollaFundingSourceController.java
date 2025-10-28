package com.example.cube.controller;

import com.example.cube.dto.request.CreateFundingSourceRequest;
import com.example.cube.dto.request.VerifyMicroDepositsRequest;
import com.example.cube.dto.response.FundingSourceResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.DwollaFundingSourceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dwolla/funding-sources")
public class DwollaFundingSourceController {

    private final DwollaFundingSourceService dwollaFundingSourceService;
    private final AuthenticationService authenticationService;

    @Autowired
    public DwollaFundingSourceController(
            DwollaFundingSourceService dwollaFundingSourceService,
            AuthenticationService authenticationService) {
        this.dwollaFundingSourceService = dwollaFundingSourceService;
        this.authenticationService = authenticationService;
    }

    // ==================== CREATE FUNDING SOURCE ====================

    @PostMapping("/create")
    public ResponseEntity<FundingSourceResponse> createFundingSource(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateFundingSourceRequest request) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("🏦 Adding bank account for user: " + userId);

            FundingSourceResponse response = dwollaFundingSourceService.createFundingSource(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to create funding source: " + e.getMessage());
            throw new RuntimeException("Failed to create funding source: " + e.getMessage());
        }
    }

    // ==================== LIST FUNDING SOURCES ====================

    @GetMapping("/list")
    public ResponseEntity<List<FundingSourceResponse>> listFundingSources(
            @RequestHeader("Authorization") String authHeader) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            List<FundingSourceResponse> fundingSources = dwollaFundingSourceService.listFundingSources(userId);

            return ResponseEntity.ok(fundingSources);

        } catch (Exception e) {
            System.err.println("❌ Failed to list funding sources: " + e.getMessage());
            throw new RuntimeException("Failed to list funding sources: " + e.getMessage());
        }
    }

    // ==================== GET FUNDING SOURCE DETAILS ====================

    @GetMapping("/{fundingSourceId}")
    public ResponseEntity<FundingSourceResponse> getFundingSource(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String fundingSourceId) {

        try {
            authenticationService.validateAndExtractUserId(authHeader);
            FundingSourceResponse response = dwollaFundingSourceService.getFundingSource(fundingSourceId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to get funding source: " + e.getMessage());
            throw new RuntimeException("Failed to get funding source: " + e.getMessage());
        }
    }

    // ==================== INITIATE MICRO-DEPOSITS ====================

    @PostMapping("/{fundingSourceId}/micro-deposits/initiate")
    public ResponseEntity<FundingSourceResponse> initiateMicroDeposits(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String fundingSourceId) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("💰 Initiating micro-deposits for funding source: " + fundingSourceId);

            FundingSourceResponse response = dwollaFundingSourceService.initiateMicroDeposits(userId, fundingSourceId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to initiate micro-deposits: " + e.getMessage());
            throw new RuntimeException("Failed to initiate micro-deposits: " + e.getMessage());
        }
    }

    // ==================== VERIFY MICRO-DEPOSITS ====================

    @PostMapping("/{fundingSourceId}/micro-deposits/verify")
    public ResponseEntity<FundingSourceResponse> verifyMicroDeposits(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String fundingSourceId,
            @Valid @RequestBody VerifyMicroDepositsRequest request) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("✅ Verifying micro-deposits for funding source: " + fundingSourceId);

            FundingSourceResponse response = dwollaFundingSourceService.verifyMicroDeposits(
                    userId, fundingSourceId, request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to verify micro-deposits: " + e.getMessage());
            throw new RuntimeException("Failed to verify micro-deposits: " + e.getMessage());
        }
    }

    // ==================== REMOVE FUNDING SOURCE ====================

    @DeleteMapping("/{fundingSourceId}")
    public ResponseEntity<Map<String, String>> removeFundingSource(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String fundingSourceId) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("🗑️ Removing funding source: " + fundingSourceId);

            dwollaFundingSourceService.removeFundingSource(userId, fundingSourceId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Funding source removed successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to remove funding source: " + e.getMessage());
            throw new RuntimeException("Failed to remove funding source: " + e.getMessage());
        }
    }

    // ==================== GET PRIMARY VERIFIED FUNDING SOURCE ====================

    @GetMapping("/primary")
    public ResponseEntity<FundingSourceResponse> getPrimaryFundingSource(
            @RequestHeader("Authorization") String authHeader) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            FundingSourceResponse response = dwollaFundingSourceService.getPrimaryVerifiedFundingSource(userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to get primary funding source: " + e.getMessage());
            throw new RuntimeException("Failed to get primary funding source: " + e.getMessage());
        }
    }
}