package com.example.cube.dto.response;

import java.util.Map;
import java.util.UUID;

public class InviteMembersResponseDTO {

    private UUID cubeId;
    private Map<String, String> results;  // userId -> status

    // Getters and Setters
    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }
}