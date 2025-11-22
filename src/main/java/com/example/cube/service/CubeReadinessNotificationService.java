package com.example.cube.service;

import java.util.UUID;

public interface CubeReadinessNotificationService {
    
    /**
     * Check if a cube is ready to start and send notifications if it is.
     * A cube is ready when:
     * 1. All expected members have joined
     * 2. All members have paid (status_id = 2)
     * 3. Cube is still in draft status (status_id = 1)
     * 
     * Sends:
     * - Admin notification: "Cube is ready to start"
     * - Member notification: "Everything is ready, waiting on admin to start"
     * 
     * @param cubeId The cube ID to check
     */
    void checkAndNotifyIfReady(UUID cubeId);
}

