package com.example.cube.dto.response.auth;

public class SignUpAuthResponse {
    private boolean success;
    private String message;
    private String userId;
    private String token; // Supabase JWT access_token

    public SignUpAuthResponse(boolean success, String message, String userId, String token) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.token = token;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUserId() { return userId; }
    public String getToken() { return token; }
}
