package com.example.cube.controller;

import com.example.cube.dto.request.CubeRequestDTO;
import com.example.cube.dto.response.CubeResponseDTO;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Cube-related endpoints.
 * Exposes REST APIs to create and manage Cube records.
 */
@RestController
@RequestMapping("/cube")
public class CubeController {

    private final CubeService cubeService;
    private final CubeMapper cubeMapper;

    @Autowired
    public CubeController(CubeService cubeService, CubeMapper cubeMapper) {
        this.cubeService = cubeService;
        this.cubeMapper = cubeMapper;
    }

    /**
     * POST /cube/create
     * Creates a new Cube entry in Supabase via JPA.
     *
     * Example JSON body:
     * {
     *   "name": "Monthly Group",
     *   "description": "Test cube",
     *   "user_id": "a68b5f92-6de0-4e7a-9c2c-d42b39b8a820",
     *   "amountPerCycle": 50.0,
     *   "duration": 1,
     *   "numberofmembers": 5,
     *   "startDate": "2025-10-11T10:00:00Z",
     *   "currency": "USD"
     * }
     */

    @PostMapping("/create")
    public ResponseEntity<?> createCube(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @RequestBody CubeRequestDTO cubeRequestDTO) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid token");
        }

        Cube savedCube = cubeService.createCubeFromDTO(cubeRequestDTO);
        CubeResponseDTO response = cubeMapper.toResponse(savedCube);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}
