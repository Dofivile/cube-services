package com.example.cube.controller;

import com.example.cube.dto.request.CreatePaymentIntentRequest;
import com.example.cube.dto.response.PaymentIntentResponse;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.StripeCardPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stripe/card")
public class StripeCardPaymentController {

    private final StripeCardPaymentService stripeCardPaymentService;
    private final AuthenticationService authenticationService;
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public StripeCardPaymentController(
            StripeCardPaymentService stripeCardPaymentService,
            AuthenticationService authenticationService,
            UserDetailsRepository userDetailsRepository) {
        this.stripeCardPaymentService = stripeCardPaymentService;
        this.authenticationService = authenticationService;
        this.userDetailsRepository = userDetailsRepository;
    }

    /**
     * Create a PaymentIntent for card payments
     * Endpoint: POST /api/stripe/card/create-payment-intent
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createCardPaymentIntent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreatePaymentIntentRequest request) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);

            // Enforce onboarding before payment: user must have a Stripe connected account
            var user = userDetailsRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean payoutsEnabled = user.getStripePayoutsEnabled() != null && user.getStripePayoutsEnabled();
            if (user.getStripeAccountId() == null || !payoutsEnabled) {
                return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).body(
                        Map.of(
                                "error", "Onboarding required",
                                "message", "Complete Stripe onboarding before making a payment"
                        )
                );
            }

            PaymentIntentResponse response = stripeCardPaymentService.createCardPaymentIntent(
                    userId,
                    request.getCubeId(),
                    request.getMemberId(),
                    request.getCycleNumber()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error creating card payment intent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                            "error", "Payment intent creation failed",
                            "message", e.getMessage()
                    )
            );
        }
    }

    /**
     * Check payment status for a specific cycle
     * Endpoint: GET /api/stripe/card/payment-status
     */
    @GetMapping("/payment-status")
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam UUID cubeId,
            @RequestParam UUID memberId,
            @RequestParam Integer cycleNumber) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);

            // You can implement this method in your service if needed
            // For now, this is a simple placeholder
            Map<String, Object> response = new HashMap<>();
            response.put("cubeId", cubeId);
            response.put("memberId", memberId);
            response.put("cycleNumber", cycleNumber);
            response.put("message", "Payment status check endpoint");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of(
                            "error", "Failed to check payment status",
                            "message", e.getMessage()
                    )
            );
        }
    }
}