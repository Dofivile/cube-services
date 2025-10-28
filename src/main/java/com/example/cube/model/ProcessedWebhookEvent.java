package com.example.cube.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_webhook_events", schema = "public")
public class ProcessedWebhookEvent {

    @Id
    @Column(name = "event_id", length = 255)
    private String eventId;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();

    @Column(name = "webhook_payload", columnDefinition = "TEXT")
    private String webhookPayload;

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public String getWebhookPayload() { return webhookPayload; }
    public void setWebhookPayload(String webhookPayload) { this.webhookPayload = webhookPayload; }
}