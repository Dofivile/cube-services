package com.example.cube.service;

import com.example.cube.dto.response.NotificationResponse;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    
    /**
     * Create a notification for a user
     * @param userId User ID who receives the notification
     * @param cubeId Cube ID (optional, can be null)
     * @param typeKey Notification type key (e.g., "cube_ready_admin")
     * @param title Notification title
     * @param body Notification body
     * @return Created notification
     */
    NotificationResponse createNotification(UUID userId, UUID cubeId, String typeKey, String title, String body);
    
    /**
     * Get all notifications for a user
     * @param userId User ID
     * @param unreadOnly If true, only return unread notifications
     * @return List of notifications
     */
    List<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly);
    
    /**
     * Mark a notification as read
     * @param notificationId Notification ID
     * @param userId User ID (for security check)
     */
    void markAsRead(UUID notificationId, UUID userId);
    
    /**
     * Get unread notification count for a user
     * @param userId User ID
     * @return Count of unread notifications
     */
    long getUnreadCount(UUID userId);
}

