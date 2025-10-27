package com.example.cube.service;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.model.Cube;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Cube-related business operations.
 * Defines all available Cube actions.
 */
public interface CubeService {

    // Create a new Cube from DTO (used by frontend requests)
    Cube createCubeFromDTO(CreateCubeRequest createCubeRequest);

    // Get all cube IDs for a user
    List<UUID> getUserCubeIds(UUID userId);

    // Retrieve a single Cube by ID
    Cube getCubeById(UUID cubeId);

}
