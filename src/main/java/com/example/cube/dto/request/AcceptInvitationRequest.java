package com.example.cube.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AcceptInvitationRequest {

    @NotBlank(message = "Invitation token is required")
    private String inviteToken;

    // Getters and Setters
    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }
}