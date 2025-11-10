package com.example.cube.controller;

import com.example.cube.dto.response.BankAccountStatusResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-account")
public class BankAccountController {

    private final BankAccountService bankAccountService;
    private final AuthenticationService authenticationService;

    @Autowired
    public BankAccountController(BankAccountService bankAccountService,
                                 AuthenticationService authenticationService) {
        this.bankAccountService = bankAccountService;
        this.authenticationService = authenticationService;
    }

    /**
     * Create a SetupIntent for linking bank account via Financial Connections
     * Frontend uses this client_secret to initialize Stripe.js
     */
    @PostMapping("/create-setup-intent")
    public ResponseEntity<Map<String, String>> createSetupIntent(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        String clientSecret = bankAccountService.createSetupIntentForBankAccount(userId);

        return ResponseEntity.ok(Map.of(
                "clientSecret", clientSecret,
                "message", "Use this client secret to link bank account"
        ));
    }

    /**
     * Save bank account details after successful setup
     * Called from frontend after SetupIntent succeeds
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveBankAccount(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> payload) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        String paymentMethodId = payload.get("paymentMethodId");

        if (paymentMethodId == null || paymentMethodId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Payment method ID is required"
            ));
        }

        bankAccountService.saveBankAccountDetails(userId, paymentMethodId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bank account linked successfully"
        ));
    }

    /**
     * Check if user has a bank account linked
     */
    @GetMapping("/status")
    public ResponseEntity<BankAccountStatusResponse> getBankAccountStatus(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        BankAccountStatusResponse status = bankAccountService.getBankAccountStatus(userId);

        return ResponseEntity.ok(status);
    }
}