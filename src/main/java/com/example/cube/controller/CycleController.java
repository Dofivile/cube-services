package com.example.cube.controller;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.dto.request.StartCubeRequest;
import com.example.cube.dto.response.CreateCubeResponse;
import com.example.cube.dto.response.CycleProcessDTO;
import com.example.cube.dto.response.CycleStatusDTO;
import com.example.cube.dto.response.StartCubeResponse;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.CycleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cubes")
public class CycleController {

    private final CycleService cycleService;
    private final CubeMapper cubeMapper;
    private final AuthenticationService authenticationService;

    @Autowired
    public CycleController(CycleService cycleService,CubeMapper cubeMapper,AuthenticationService authenticationService) {
        this.cycleService = cycleService;
        this.cubeMapper = cubeMapper;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartCubeResponse> startCube(@RequestHeader("Authorization") String authHeader, @RequestBody StartCubeRequest startCubeRequest) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        Cube cube = cycleService.startCube(startCubeRequest.getCubeId(), userId);
        StartCubeResponse response = cubeMapper.toStartCubeResponse(cube);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cubeId}/cycles/process")
    public ResponseEntity<CycleProcessDTO> processCycle(@PathVariable UUID cubeId, @RequestHeader("Authorization") String authHeader) {
        authenticationService.validateAndExtractUserId(authHeader);
        CycleProcessDTO result = cycleService.processCycle(cubeId);
        return ResponseEntity.ok(result);
    }
}