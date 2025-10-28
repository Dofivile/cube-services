package com.example.cube.controller;

import com.example.cube.dto.request.PlaidExchangeRequest;
import com.example.cube.dto.response.PlaidLinkResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.PlaidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {

    private final PlaidService plaidService;
    private final AuthenticationService authenticationService;

    @Autowired
    public PlaidController(PlaidService plaidService, AuthenticationService authenticationService) {
        this.plaidService = plaidService;
        this.authenticationService = authenticationService;
    }

    /**
     * Create Plaid Link token for frontend to initialize Plaid Link
     */
    @PostMapping("/create-link-token")
    public ResponseEntity<Map<String, String>> createLinkToken(
            @RequestHeader("Authorization") String authHeader) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            String linkToken = plaidService.createLinkToken(userId);

            Map<String, String> response = new HashMap<>();
            response.put("link_token", linkToken);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to create link token: " + e.getMessage());
            throw new RuntimeException("Failed to create link token: " + e.getMessage());
        }
    }

    /**
     * Exchange Plaid public token and link bank to Dwolla
     */
    @PostMapping("/exchange")
    public ResponseEntity<PlaidLinkResponse> exchangeToken(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PlaidExchangeRequest request) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("🔗 Linking bank via Plaid for user: " + userId);

            PlaidLinkResponse response = plaidService.exchangeTokenAndLinkToDwolla(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to link bank: " + e.getMessage());
            throw new RuntimeException("Failed to link bank: " + e.getMessage());
        }
    }
}