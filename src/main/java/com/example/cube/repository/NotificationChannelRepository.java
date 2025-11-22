package com.example.cube.repository;

import com.example.cube.model.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Integer> {
    
    /**
     * Find notification channel by its name
     * @param channelName e.g., "email", "in_app", "push"
     * @return Optional NotificationChannel
     */
    Optional<NotificationChannel> findByChannelName(String channelName);
}

