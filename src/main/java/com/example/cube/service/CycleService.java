package com.example.cube.service;

import com.example.cube.dto.response.CycleProcessDTO;
import com.example.cube.dto.response.CycleStatusDTO;
import com.example.cube.model.Cube;

import java.util.Map;
import java.util.UUID;

public interface CycleService {

    // Start a cube (transition from draft/pending to active)
    Cube startCube(UUID cubeId, UUID userId);

    // Process current cycle (collect payments, select winner, payout)
    CycleProcessDTO processCycle(UUID cubeId);
}
