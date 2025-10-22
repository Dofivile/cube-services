package com.example.cube.controller;

import com.example.cube.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Autowired
    private StripePaymentService stripePaymentService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,  @RequestHeader("Stripe-Signature") String sigHeader) {
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
            default:
                System.out.println("ℹ️ Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok("Success");
    }

    private void handlePaymentIntentSucceeded(Event event) {
        try {
            System.out.println("🔍 Processing payment_intent.succeeded...");

            // Access the stripe object directly from event data
            com.stripe.model.StripeObject stripeObject = event.getData().getObject();

            // Cast to PaymentIntent
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
}