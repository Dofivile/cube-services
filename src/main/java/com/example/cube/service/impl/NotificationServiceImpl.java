package com.example.cube.service.impl;

import com.example.cube.dto.response.NotificationResponse;
import com.example.cube.mapper.NotificationMapper;
import com.example.cube.model.Notification;
import com.example.cube.model.NotificationType;
import com.example.cube.repository.NotificationRepository;
import com.example.cube.repository.NotificationTypeRepository;
import com.example.cube.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationTypeRepository notificationTypeRepository,
                                   NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationTypeRepository = notificationTypeRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public NotificationResponse createNotification(UUID userId, UUID cubeId, String typeKey, String title, String body) {
        // Find the notification type
        NotificationType type = notificationTypeRepository.findByTypeKey(typeKey)
                .orElseThrow(() -> new RuntimeException("Notification type not found: " + typeKey));

        // Create the notification
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setCubeId(cubeId);
        notification.setTypeId(type.getTypeId());
        notification.setTitle(title);
        notification.setBody(body);
        notification.setStatus("queued");
        notification.setCreatedAt(Instant.now());

        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly) {
        List<Notification> notifications;
        
        if (unreadOnly) {
            notifications = notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        // Security check: ensure the notification belongs to this user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Notification does not belong to this user");
        }

        // Mark as read if not already read
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId).size();
    }
}

