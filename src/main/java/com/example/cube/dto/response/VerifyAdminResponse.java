package com.example.cube.dto.response;

public class VerifyAdminResponse {

    private boolean isAdmin;

    public VerifyAdminResponse() {}

    public VerifyAdminResponse(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    // Getter and Setter
    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}