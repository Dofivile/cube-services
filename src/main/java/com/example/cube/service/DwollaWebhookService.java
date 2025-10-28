package com.example.cube.service;

import java.util.Map;

public interface DwollaWebhookService {

    /**
     * Validate webhook signature
     */
    boolean validateWebhookSignature(String payload, String signature);

    /**
     * Check if event has already been processed
     */
    boolean isEventProcessed(String eventId);

    /**
     * Process webhook event
     */
    void processWebhookEvent(Map<String, Object> webhook);

    /**
     * Mark event as processed
     */
    void markEventAsProcessed(String eventId, String topic, String resourceId, String payload);
}