package com.example.cube.repository;

import com.example.cube.model.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    
    /**
     * Find all delivery attempts for a specific notification
     * @param notificationId Notification ID
     * @return List of delivery records
     */
    List<NotificationDelivery> findByNotificationId(UUID notificationId);
    
    /**
     * Find delivery record for a specific notification and channel
     * @param notificationId Notification ID
     * @param channelId Channel ID
     * @return List of delivery records (should be 0 or 1)
     */
    List<NotificationDelivery> findByNotificationIdAndChannelId(UUID notificationId, Integer channelId);
}

