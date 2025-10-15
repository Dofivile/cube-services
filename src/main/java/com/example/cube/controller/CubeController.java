package com.example.cube.controller;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.dto.request.GetUserCubesRequest;
import com.example.cube.dto.response.CreateCubeResponse;
import com.example.cube.dto.response.GetUserCubesResponse;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public CubeController(CubeService cubeService, CubeMapper cubeMapper, AuthenticationService authenticationService) {
        this.cubeService = cubeService;
        this.cubeMapper = cubeMapper;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateCubeResponse> createCube(@RequestHeader("Authorization") String authHeader,
                                                         @RequestBody CreateCubeRequest createCubeRequest) {
        // Validate and extract user ID (throws UnauthorizedException if invalid)
        authenticationService.validateAndExtractUserId(authHeader);

        Cube savedCube = cubeService.createCubeFromDTO(createCubeRequest);
        CreateCubeResponse response = cubeMapper.toResponse(savedCube);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/listCubes")
    public ResponseEntity<GetUserCubesResponse> getUserCubes(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody GetUserCubesRequest request) {

        // Validate auth token
        authenticationService.validateAndExtractUserId(authHeader);

        // Get cube IDs for the user
        List<UUID> cubeIds = cubeService.getUserCubeIds(request.getUser_id());

        // Build response
        GetUserCubesResponse response = new GetUserCubesResponse(request.getUser_id(), cubeIds);

        return ResponseEntity.ok(response);
    }
}