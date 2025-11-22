package com.example.cube.controller;

import com.example.cube.dto.response.NotificationResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticationService authenticationService;

    public NotificationController(NotificationService notificationService,
                                  AuthenticationService authenticationService) {
        this.notificationService = notificationService;
        this.authenticationService = authenticationService;
    }

    /**
     * Get all notifications for the authenticated user
     * 
     * GET /api/notifications?unreadOnly=false
     * 
     * @param authHeader Authorization header with Bearer token
     * @param unreadOnly If true, only return unread notifications
     * @return List of notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "unreadOnly", defaultValue = "false") boolean unreadOnly) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId, unreadOnly);
        
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a notification as read
     * 
     * POST /api/notifications/{id}/read
     * 
     * @param authHeader Authorization header with Bearer token
     * @param notificationId Notification ID to mark as read
     * @return Success response
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") UUID notificationId) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        notificationService.markAsRead(notificationId, userId);
        
        return ResponseEntity.ok(Map.of(
                "message", "Notification marked as read",
                "notificationId", notificationId.toString()
        ));
    }

    /**
     * Get unread notification count for the authenticated user
     * 
     * GET /api/notifications/unread-count
     * 
     * @param authHeader Authorization header with Bearer token
     * @return Count of unread notifications
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
}

