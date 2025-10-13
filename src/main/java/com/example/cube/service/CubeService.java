package com.example.cube.service;

import com.example.cube.dto.request.CubeRequestDTO;
import com.example.cube.model.Cube;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Cube-related business operations.
 * Defines all available Cube actions.
 */
public interface CubeService {

    // Create a new Cube from DTO (used by frontend requests)
    Cube createCubeFromDTO(CubeRequestDTO cubeRequestDTO);

    // Retrieve all Cubes
    List<Cube> getAllCubes();

    // Retrieve a single Cube by ID
    Cube getCubeById(UUID cubeId);

    // Delete a Cube
    void deleteCube(UUID cubeId);
    
}
