package com.example.cube.service;

import java.util.UUID;

public interface NotificationDeliveryService {
    
    /**
     * Deliver a notification through all specified channels (email + in-app)
     * This creates the in-app notification and sends the email
     * 
     * @param notificationId The notification ID
     * @param userEmail The user's email address
     * @param title The notification title
     * @param body The notification body
     */
    void deliverNotification(UUID notificationId, String userEmail, String title, String body);
    
    /**
     * Record a successful delivery for a channel
     * @param notificationId Notification ID
     * @param channelName Channel name (e.g., "email", "in_app")
     */
    void recordDeliverySuccess(UUID notificationId, String channelName);
    
    /**
     * Record a failed delivery for a channel
     * @param notificationId Notification ID
     * @param channelName Channel name (e.g., "email", "in_app")
     * @param error Error message
     */
    void recordDeliveryFailure(UUID notificationId, String channelName, String error);
}

