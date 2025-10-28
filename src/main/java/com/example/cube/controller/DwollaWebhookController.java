package com.example.cube.controller;

import com.example.cube.service.DwollaWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dwolla/webhooks")
public class DwollaWebhookController {

    @Autowired
    private DwollaWebhookService dwollaWebhookService;

    /**
     * Main webhook endpoint - receives webhooks from Dwolla
     * IMPORTANT: Must respond within 10 seconds with 200 status
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Request-Signature-SHA-256", required = false) String signature) {

        try {
            System.out.println("\n📨 ========== WEBHOOK RECEIVED ==========");

            // STEP 1: VALIDATE SIGNATURE (Authentication)
            if (signature == null || signature.isEmpty()) {
                System.err.println("❌ Missing webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
            }

            if (!dwollaWebhookService.validateWebhookSignature(rawPayload, signature)) {
                System.err.println("❌ Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            System.out.println("✅ Webhook signature validated");

            // STEP 2: PARSE WEBHOOK PAYLOAD
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> webhook = mapper.readValue(rawPayload, Map.class);

            String eventId = (String) webhook.get("id");
            String topic = (String) webhook.get("topic");

            // STEP 3: CHECK FOR DUPLICATE EVENTS
            if (dwollaWebhookService.isEventProcessed(eventId)) {
                System.out.println("⚠️ Duplicate event detected: " + eventId);
                // Still return 200 to prevent retries
                return ResponseEntity.ok("Event already processed");
            }

            // STEP 4: PROCESS WEBHOOK (async recommended in production)
            // For now, processing synchronously but should be queued in production
            dwollaWebhookService.processWebhookEvent(webhook);

            // STEP 5: RETURN 200 IMMEDIATELY (must be within 10 seconds)
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error handling webhook: " + e.getMessage());
            e.printStackTrace();

            // Still return 200 to prevent unnecessary retries for our errors
            // Log the error for manual investigation
            return ResponseEntity.ok("Webhook received but processing failed");
        }
    }

    /**
     * Health check endpoint for webhook subscription
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Webhook endpoint is healthy");
    }
}