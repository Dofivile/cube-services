package com.example.cube.controller;

import com.example.cube.dto.request.CubeRequestDTO;
import com.example.cube.dto.response.CubeResponseDTO;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for Cube-related endpoints.
 * Exposes REST APIs to create and manage Cube records.
 */
@RestController
@RequestMapping("/api/cubes")
public class CubeController {

    private final CubeService cubeService;
    private final CubeMapper cubeMapper;
    private final AuthenticationService authenticationService;

    @Autowired
    public CubeController(CubeService cubeService,
                          CubeMapper cubeMapper,
                          AuthenticationService authenticationService) {
        this.cubeService = cubeService;
        this.cubeMapper = cubeMapper;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/create")
    public ResponseEntity<CubeResponseDTO> createCube(@RequestHeader("Authorization") String authHeader,
            @RequestBody CubeRequestDTO cubeRequestDTO) {

        // Validate and extract user ID (throws UnauthorizedException if invalid)
        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        Cube savedCube = cubeService.createCubeFromDTO(cubeRequestDTO);
        CubeResponseDTO response = cubeMapper.toResponse(savedCube);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}