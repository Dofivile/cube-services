package com.example.cube.mapper;

import com.example.cube.dto.response.NotificationResponse;
import com.example.cube.model.Notification;
import com.example.cube.model.NotificationType;
import com.example.cube.repository.NotificationTypeRepository;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    private final NotificationTypeRepository notificationTypeRepository;

    public NotificationMapper(NotificationTypeRepository notificationTypeRepository) {
        this.notificationTypeRepository = notificationTypeRepository;
    }

    /**
     * Convert Notification entity to NotificationResponse DTO
     */
    public NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(notification.getNotificationId());
        response.setCubeId(notification.getCubeId());
        response.setTypeId(notification.getTypeId());
        
        // Fetch the type key for frontend display
        notificationTypeRepository.findById(notification.getTypeId())
                .ifPresent(type -> response.setTypeKey(type.getTypeKey()));
        
        response.setTitle(notification.getTitle());
        response.setBody(notification.getBody());
        response.setStatus(notification.getStatus());
        response.setSentAt(notification.getSentAt());
        response.setReadAt(notification.getReadAt());
        response.setCreatedAt(notification.getCreatedAt());
        
        return response;
    }
}

