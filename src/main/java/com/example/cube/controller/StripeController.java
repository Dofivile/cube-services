package com.example.cube.controller;

import com.example.cube.dto.request.CreatePaymentIntentRequest;
import com.example.cube.dto.response.PaymentIntentResponse;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.BankAccountService;
import com.example.cube.service.PayoutService;
import com.example.cube.service.StripeConnectService;
import com.example.cube.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.Stripe;
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
    private final BankAccountService bankAccountService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    public StripeController(
            StripePaymentService stripePaymentService,
            StripeConnectService stripeConnectService,
            PayoutService payoutService,
            AuthenticationService authenticationService,
            UserDetailsRepository userDetailsRepository,
            BankAccountService bankAccountService) {
        this.stripePaymentService = stripePaymentService;
        this.stripeConnectService = stripeConnectService;
        this.payoutService = payoutService;
        this.authenticationService = authenticationService;
        this.userDetailsRepository = userDetailsRepository;
        this.bankAccountService = bankAccountService;
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
            case "payment_intent.processing":
                handlePaymentIntentProcessing(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            case "charge.succeeded":
                handleChargeSucceeded(event);
                break;
            case "account.updated":
                handleAccountUpdated(event);
                break;
            case "setup_intent.succeeded":
                handleSetupIntentSucceeded(event);
                break;
            default:
                System.out.println("ℹ️ Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    // ==================== PRIVATE HELPERS ====================

    private void handlePaymentIntentSucceeded(Event event) {
        try {
            var deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isEmpty()) return;
            PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();

            System.out.println("🔍 Processing payment_intent.succeeded...");
            System.out.println("  Payment Intent ID: " + paymentIntent.getId());
            System.out.println("  Status: " + paymentIntent.getStatus());
            System.out.println("  Amount: " + paymentIntent.getAmount());

            stripePaymentService.handlePaymentIntentSucceeded(paymentIntent.getId());
            System.out.println("✅ Payment intent processed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error handling payment_intent.succeeded: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePaymentIntentProcessing(Event event) {
        try {
            var deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isEmpty()) return;
            PaymentIntent paymentIntent = (PaymentIntent) deserializer.getObject().get();

            System.out.println("🔍 Processing payment_intent.processing...");
            System.out.println("  Payment Intent ID: " + paymentIntent.getId());
            System.out.println("  Amount: $" + (paymentIntent.getAmount() / 100.0));
            System.out.println("  ⏳ ACH payment processing - will settle in 1-2 business days");

        } catch (Exception e) {
            System.err.println("❌ Error handling payment_intent.processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleChargeSucceeded(Event event) {
        try {
            System.out.println("🔍 Processing charge.succeeded (start)...");
            System.out.println("  Event ID: " + event.getId());

            var deserializer = event.getDataObjectDeserializer();
            System.out.println("  Deserializer created: " + deserializer);
            System.out.println("  Object present: " + !deserializer.getObject().isEmpty());

            if (deserializer.getObject().isEmpty()) {
                System.err.println("⚠️ Charge object is empty - deserialization failed!");
                System.err.println("  Event data: " + event.getData());
                return;
            }

            Charge charge = (Charge) deserializer.getObject().get();

            System.out.println("🔍 Processing charge.succeeded...");
            System.out.println("  Charge ID: " + charge.getId());
            System.out.println("  Payment Intent: " + charge.getPaymentIntent());

            if (charge.getPaymentIntent() != null) {
                stripePaymentService.handlePaymentIntentSucceeded(charge.getPaymentIntent());
            }

            System.out.println("✅ Charge reconciled via PaymentIntent: " + charge.getPaymentIntent());

        } catch (Exception e) {
            System.err.println("❌ Error handling charge.succeeded: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSetupIntentSucceeded(Event event) {
        try {
            var deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isEmpty()) return;
            SetupIntent setupIntent = (SetupIntent) deserializer.getObject().get();

            System.out.println("🔍 Processing setup_intent.succeeded...");
            String paymentMethodId = setupIntent.getPaymentMethod();
            String userId = setupIntent.getMetadata().get("user_id");

            if (userId != null && paymentMethodId != null) {
                bankAccountService.saveBankAccountDetails(UUID.fromString(userId), paymentMethodId);
                System.out.println("✅ Bank account linked via webhook");
            }

        } catch (Exception e) {
            System.err.println("❌ Error handling setup_intent.succeeded: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleAccountUpdated(Event event) {
        try {
            var deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isEmpty()) return;
            Account account = (Account) deserializer.getObject().get();

            System.out.println("🔍 Processing account.updated...");
            String accountId = account.getId();
            System.out.println("  Account ID: " + accountId);

            stripeConnectService.updateAccountStatus(accountId);
            System.out.println("✅ Account status updated successfully");

        } catch (Exception e) {
            System.err.println("❌ Error handling account.updated: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        System.out.println("❌ Payment failed: " + event.getId());
    }
}