package com.example.cube.repository;

import com.example.cube.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first)
     * @param userId User ID
     * @return List of notifications
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find unread notifications for a user (where readAt is null)
     * @param userId User ID
     * @return List of unread notifications
     */
    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Check if a notification already exists for a cube and type (for idempotency)
     * @param cubeId Cube ID
     * @param typeId Notification type ID
     * @return true if exists
     */
    boolean existsByCubeIdAndTypeId(UUID cubeId, Integer typeId);
}

