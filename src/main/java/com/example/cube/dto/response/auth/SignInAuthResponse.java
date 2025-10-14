package com.example.cube.dto.response.auth;

/**
 * Response DTO for user sign in
 */
public class SignInAuthResponse {

    private boolean success;
    private String message;
    private String userId;
    private String token;

    public SignInAuthResponse(boolean success, String message, String userId, String token) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.token = token;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUserId() { return userId; }
    public String getToken() { return token; }
}