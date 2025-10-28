package com.example.cube.controller;

import com.example.cube.dto.request.CreatePaymentIntentRequest;
import com.example.cube.dto.response.PaymentIntentResponse;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.PayoutService;
import com.example.cube.service.StripeConnectService;
import com.example.cube.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Balance;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final StripePaymentService stripePaymentService;
    private final StripeConnectService stripeConnectService;
    private final PayoutService payoutService;
    private final AuthenticationService authenticationService;
    private final UserDetailsRepository userDetailsRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    public StripeController(
            StripePaymentService stripePaymentService,
            StripeConnectService stripeConnectService,
            PayoutService payoutService,
            AuthenticationService authenticationService,
            UserDetailsRepository userDetailsRepository) {
        this.stripePaymentService = stripePaymentService;
        this.stripeConnectService = stripeConnectService;
        this.payoutService = payoutService;
        this.authenticationService = authenticationService;
        this.userDetailsRepository = userDetailsRepository;
    }

    // ==================== PAYMENT OPERATIONS ====================

    @PostMapping("/payments/create-payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreatePaymentIntentRequest request) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        // Enforce onboarding before payment: user must have a Stripe connected account
        var user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean payoutsEnabled = user.getStripePayoutsEnabled() != null && user.getStripePayoutsEnabled();
        if (user.getStripeAccountId() == null || !payoutsEnabled) {
            throw new RuntimeException("Complete Stripe onboarding before making a payment");
        }

        PaymentIntentResponse response = stripePaymentService.createPaymentIntent(
                userId,
                request.getCubeId(),
                request.getMemberId(),
                request.getCycleNumber()
        );

        return ResponseEntity.ok(response);
    }

    // ==================== PLATFORM BALANCE (ADMIN/OPS) ====================
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getPlatformBalance(
            @RequestHeader("Authorization") String authHeader) {

        authenticationService.validateAndExtractUserId(authHeader);

        try {
            Stripe.apiKey = stripeApiKey;
            Balance bal = Balance.retrieve();

            long availableUsd = bal.getAvailable().stream()
                    .filter(m -> "usd".equalsIgnoreCase(m.getCurrency()))
                    .mapToLong(m -> m.getAmount())
                    .sum();

            long pendingUsd = bal.getPending().stream()
                    .filter(m -> "usd".equalsIgnoreCase(m.getCurrency()))
                    .mapToLong(m -> m.getAmount())
                    .sum();

            return ResponseEntity.ok(Map.of(
                    "availableUsd", availableUsd / 100.0,
                    "pendingUsd", pendingUsd / 100.0
            ));

        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to retrieve Stripe balance",
                    "message", e.getMessage()
            ));
        }
    }

    

    // ==================== ONBOARDING OPERATIONS ====================

    @PostMapping("/onboarding/initiate")
    public ResponseEntity<Map<String, String>> initiateOnboarding(@RequestHeader("Authorization") String authHeader) {
        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        System.out.println("📝 Initiating Stripe Connect onboarding for user: " + userId);
        String onboardingUrl = stripeConnectService.createConnectedAccountAndGetOnboardingLink(userId);

        Map<String, String> response = new HashMap<>();
        response.put("onboardingUrl", onboardingUrl);
        response.put("message", "Redirect user to this URL to complete Stripe Connect onboarding");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/onboarding/status")
    public ResponseEntity<Map<String, Object>> getOnboardingStatus(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        var user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("hasStripeAccount", user.getStripeAccountId() != null);
        response.put("payoutsEnabled", user.getStripePayoutsEnabled() != null ? user.getStripePayoutsEnabled() : false);
        response.put("stripeAccountId", user.getStripeAccountId());

        return ResponseEntity.ok(response);
    }

    // Simple check endpoint for frontend UX
    @GetMapping("/onboarding/check")
    public ResponseEntity<Map<String, Object>> checkOnboarding(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        var user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasAccount = user.getStripeAccountId() != null;
        boolean payoutsEnabled = user.getStripePayoutsEnabled() != null && user.getStripePayoutsEnabled();
        boolean onboarded = hasAccount && payoutsEnabled;

        String message = onboarded
                ? "Stripe account is set up and payouts are enabled"
                : "You have not set up your Stripe account";

        return ResponseEntity.ok(Map.of(
                "onboarded", onboarded,
                "hasStripeAccount", hasAccount,
                "payoutsEnabled", payoutsEnabled,
                "message", message
        ));
    }

    // ==================== PAYOUT OPERATIONS ====================

    @PostMapping("/payouts/send")
    public ResponseEntity<Map<String, Object>> sendPayout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            UUID winnerId = UUID.fromString(payload.get("winnerId").toString());
            UUID cubeId = UUID.fromString(payload.get("cubeId").toString());
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());
            Integer cycle = (Integer) payload.get("cycle");

            UUID payoutId = payoutService.sendPayoutToWinner(winnerId, amount, cubeId, cycle);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "payoutId", payoutId,
                    "message", "Payout processed successfully"
            ));

        } catch (Exception e) {
            System.err.println("❌ Payout failed: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Payout failed: " + e.getMessage()
            ));
        }
    }
    // ==================== WEBHOOK HANDLING ====================

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Verify webhook signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("❌ Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        System.out.println("📨 Webhook received: " + event.getType());

        // Handle events
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            case "account.updated":
                handleAccountUpdated(event);
                break;
            default:
                System.out.println("ℹ️ Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private void handlePaymentIntentSucceeded(Event event) {
        try {
            System.out.println("🔍 Processing payment_intent.succeeded...");

            com.stripe.model.StripeObject stripeObject = event.getData().getObject();
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;

            String paymentIntentId = paymentIntent.getId();
            String status = paymentIntent.getStatus();
            Long amount = paymentIntent.getAmount();

            System.out.println("  Payment Intent ID: " + paymentIntentId);
            System.out.println("  Status: " + status);
            System.out.println("  Amount: " + amount);

            stripePaymentService.handlePaymentIntentSucceeded(paymentIntentId);

            System.out.println("✅ Payment intent processed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error handling payment_intent.succeeded: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        System.out.println("❌ Payment failed: " + event.getId());
        // TODO: Handle failed payments - notify user, update status
    }

    private void handleAccountUpdated(Event event) {
        try {
            System.out.println("🔍 Processing account.updated...");

            com.stripe.model.StripeObject stripeObject = event.getData().getObject();
            com.stripe.model.Account account = (com.stripe.model.Account) stripeObject;

            String accountId = account.getId();
            System.out.println("  Account ID: " + accountId);

            // Update the account status in our database
            stripeConnectService.updateAccountStatus(accountId);

            System.out.println("✅ Account status updated successfully");

        } catch (Exception e) {
            System.err.println("❌ Error handling account.updated: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
