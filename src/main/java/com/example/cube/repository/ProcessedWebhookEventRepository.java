package com.example.cube.repository;

import com.example.cube.model.ProcessedWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, String> {
    boolean existsByEventId(String eventId);
}