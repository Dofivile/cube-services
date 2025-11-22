package com.example.cube.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery", schema = "public")
public class NotificationDelivery {

    @Id
    @GeneratedValue
    @Column(name = "delivery_id", columnDefinition = "uuid")
    private UUID deliveryId;

    @Column(name = "notification_id", nullable = false, columnDefinition = "uuid")
    private UUID notificationId;

    @Column(name = "channel_id", nullable = false)
    private Integer channelId;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    // Getters and Setters
    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

