package com.example.cube.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinCubeRequest {

    @NotBlank(message = "Invitation code is required")
    @Size(min = 6, max = 6, message = "Invitation code must be exactly 6 characters")
    private String invitationCode;

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }
}