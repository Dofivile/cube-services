package com.example.cube.dto.response;

import java.util.Map;
import java.util.UUID;

public class AddMembersDirectResponse {

    private UUID cubeId;
    private Map<String, String> results;  // userId -> status
    private String message;

    public AddMembersDirectResponse() {}

    public AddMembersDirectResponse(UUID cubeId, Map<String, String> results, String message) {
        this.cubeId = cubeId;
        this.results = results;
        this.message = message;
    }

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