package com.example.cube.controller;

import com.example.cube.dto.response.TransactionHistoryResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for transaction-related endpoints
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Get transaction history for logged-in user
     * Shows all payments made and payouts received
     *
     * Endpoint: GET /api/transactions/my-history
     * Headers: Authorization: Bearer <token>
     */
    @GetMapping("/my-history")
    public ResponseEntity<TransactionHistoryResponse> getMyTransactionHistory(
            @RequestHeader("Authorization") String authHeader) {

        // Validate token and extract user ID
        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        // Get transaction history
        TransactionHistoryResponse response = transactionService.getUserTransactionHistory(userId);

        return ResponseEntity.ok(response);
    }
}