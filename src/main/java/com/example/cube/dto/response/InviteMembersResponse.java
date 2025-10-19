package com.example.cube.dto.response;

import java.util.Map;
import java.util.UUID;

public class InviteMembersResponse {

    private UUID cubeId;
    private Map<String, String> results;  // email -> status
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}