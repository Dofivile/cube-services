package com.example.cube.service.impl;

import com.example.cube.model.ProcessedWebhookEvent;
import com.example.cube.repository.ProcessedWebhookEventRepository;
import com.example.cube.service.DwollaTransferService;
import com.example.cube.service.DwollaWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Service
public class DwollaWebhookServiceImpl implements DwollaWebhookService {

    @Value("${DWOLLA_WEBHOOK_SECRET}")
    private String webhookSecret;

    @Autowired
    private ProcessedWebhookEventRepository processedWebhookEventRepository;

    @Autowired
    private DwollaTransferService dwollaTransferService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean validateWebhookSignature(String payload, String signature) {
        try {
            // Create HMAC SHA-256 signature
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String calculatedSignature = hexString.toString();

            // Compare signatures (constant-time comparison to prevent timing attacks)
            return constantTimeEquals(calculatedSignature, signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.err.println("❌ Error validating webhook signature: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isEventProcessed(String eventId) {
        return processedWebhookEventRepository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public void processWebhookEvent(Map<String, Object> webhook) {
        String eventId = (String) webhook.get("id");
        String topic = (String) webhook.get("topic");
        String resourceId = (String) webhook.get("resourceId");

        System.out.println("\n📨 ========== PROCESSING WEBHOOK ==========");
        System.out.println("Event ID: " + eventId);
        System.out.println("Topic: " + topic);
        System.out.println("Resource ID: " + resourceId);

        // Check if already processed
        if (isEventProcessed(eventId)) {
            System.out.println("⚠️ Event already processed, skipping...");
            return;
        }

        try {
            // Extract resource URL from _links
            Map<String, Object> links = (Map<String, Object>) webhook.get("_links");
            Map<String, String> resource = (Map<String, String>) links.get("resource");
            String resourceUrl = resource.get("href");
            String transferId = extractIdFromUrl(resourceUrl);

            // Handle different event topics
            switch (topic) {
                // Transfer created
                case "transfer_created":
                case "customer_transfer_created":
                    System.out.println("📤 Transfer created: " + transferId);
                    // Transfer is created, typically no action needed yet
                    break;

                // Transfer completed (funds successfully moved)
                case "transfer_completed":
                case "customer_transfer_completed":
                    System.out.println("✅ Transfer completed: " + transferId);
                    dwollaTransferService.handleTransferWebhook(transferId, "processed");
                    break;

                // Transfer failed
                case "transfer_failed":
                case "customer_transfer_failed":
                    System.err.println("❌ Transfer failed: " + transferId);
                    dwollaTransferService.handleTransferWebhook(transferId, "failed");
                    break;

                // Transfer cancelled
                case "transfer_cancelled":
                case "customer_transfer_cancelled":
                    System.out.println("⚠️ Transfer cancelled: " + transferId);
                    dwollaTransferService.handleTransferWebhook(transferId, "cancelled");
                    break;

                // Bank transfer events (ACH processing)
                case "bank_transfer_created":
                    System.out.println("🏦 Bank transfer initiated: " + transferId);
                    break;

                case "bank_transfer_completed":
                    System.out.println("✅ Bank transfer completed: " + transferId);
                    break;

                case "bank_transfer_failed":
                    System.err.println("❌ Bank transfer failed: " + transferId);
                    break;

                // Customer verification events (optional handling)
                case "customer_verified":
                    System.out.println("✅ Customer verified");
                    break;

                case "customer_reverification_needed":
                    System.out.println("⚠️ Customer needs reverification");
                    break;

                case "customer_verification_document_needed":
                    System.out.println("📄 Customer document needed");
                    break;

                case "customer_verification_document_uploaded":
                    System.out.println("📤 Customer document uploaded");
                    break;

                case "customer_verification_document_approved":
                    System.out.println("✅ Customer document approved");
                    break;

                case "customer_verification_document_failed":
                    System.err.println("❌ Customer document failed");
                    break;

                // Funding source events
                case "customer_funding_source_added":
                    System.out.println("🏦 Funding source added");
                    break;

                case "customer_funding_source_verified":
                    System.out.println("✅ Funding source verified");
                    break;

                case "customer_funding_source_removed":
                    System.out.println("🗑️ Funding source removed");
                    break;

                // Microdeposit events
                case "customer_microdeposits_added":
                    System.out.println("💰 Microdeposits sent");
                    break;

                case "customer_microdeposits_completed":
                    System.out.println("✅ Microdeposits completed");
                    break;

                case "customer_microdeposits_failed":
                    System.err.println("❌ Microdeposits failed");
                    break;

                default:
                    System.out.println("ℹ️ Unhandled event topic: " + topic);
            }

            // Mark event as processed
            String payload = objectMapper.writeValueAsString(webhook);
            markEventAsProcessed(eventId, topic, resourceId, payload);

            System.out.println("✅ Webhook processed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process webhook: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void markEventAsProcessed(String eventId, String topic, String resourceId, String payload) {
        ProcessedWebhookEvent event = new ProcessedWebhookEvent();
        event.setEventId(eventId);
        event.setTopic(topic);
        event.setResourceId(resourceId);
        event.setWebhookPayload(payload);
        processedWebhookEventRepository.save(event);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Constant-time string comparison to prevent timing attacks
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private String extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
}