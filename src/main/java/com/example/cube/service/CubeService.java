package com.example.cube.service;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.dto.response.CubeActivityResponse;
import com.example.cube.dto.response.WinnerResponse;
import com.example.cube.model.Cube;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Cube-related business operations.
 * Defines all available Cube actions.
 */
public interface CubeService {

    Cube createCubeFromDTO(CreateCubeRequest createCubeRequest, UUID userId);

    // Get all cube IDs for a user
    List<UUID> getUserCubeIds(UUID userId);

    // Retrieve a single Cube by ID
    Cube getCubeById(UUID cubeId);
    
    /**
     * Get recent activity for a cube (payments, winners, new members)
     * @param cubeId Cube ID
     * @param limit Maximum number of activities to return
     * @return List of activities sorted by most recent first
     */
    List<CubeActivityResponse> getCubeActivity(UUID cubeId, int limit);

    /**
     * Get all previous winners for a cube
     * @param cubeId Cube ID
     * @return List of winners with user details, ordered by cycle number
     */
    List<WinnerResponse> getPreviousWinners(UUID cubeId);

}
