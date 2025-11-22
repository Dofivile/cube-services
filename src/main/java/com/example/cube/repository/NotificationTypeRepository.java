package com.example.cube.repository;

import com.example.cube.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTypeRepository extends JpaRepository<NotificationType, Integer> {
    
    /**
     * Find notification type by its unique key
     * @param typeKey e.g., "cube_ready_admin", "cube_ready_member"
     * @return Optional NotificationType
     */
    Optional<NotificationType> findByTypeKey(String typeKey);
}

