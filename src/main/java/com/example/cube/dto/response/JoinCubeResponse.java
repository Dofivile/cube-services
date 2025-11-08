package com.example.cube.dto.response;

import java.util.UUID;

public class JoinCubeResponse {

    private boolean success;
    private String message;
    private UUID cubeId;
    private String cubeName;
    private UUID memberId;

    public JoinCubeResponse() {}

    public JoinCubeResponse(boolean success, String message, UUID cubeId, String cubeName, UUID memberId) {
        this.success = success;
        this.message = message;
        this.cubeId = cubeId;
        this.cubeName = cubeName;
        this.memberId = memberId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public String getCubeName() {
        return cubeName;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public void setMemberId(UUID memberId) {
        this.memberId = memberId;
    }
}