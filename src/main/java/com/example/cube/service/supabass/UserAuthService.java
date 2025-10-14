package com.example.cube.service.supabass;

import com.example.cube.dto.request.auth.SignInAuthRequest;
import com.example.cube.dto.request.auth.SignUpAuthRequest;
import com.example.cube.dto.response.auth.SignInAuthResponse;
import com.example.cube.dto.response.auth.SignUpAuthResponse;
import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class UserAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Autowired
    private UserDetailsRepository userDetailsRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sign up a new user in Supabase and save to local database
     */
    public SignUpAuthResponse signUp(SignUpAuthRequest req) {
        String url = supabaseUrl + "/auth/v1/signup";

        // Call Supabase
        JSONObject json = callSupabaseAuth(url, req.getEmail(), req.getPassword());

        // Extract user data (at root level for signup)
        String userId = json.optString("id", null);
        String accessToken = extractAccessToken(json);

        if (userId == null || userId.isEmpty()) {
            throw new RuntimeException("Failed to extract user ID from Supabase response");
        }

        // Check if email confirmation is required
        String confirmationSentAt = json.optString("confirmation_sent_at", null);
        String message = (confirmationSentAt != null)
                ? "User registered successfully. Please check your email to confirm."
                : "User registered successfully";

        // Save user to local database
        saveUserToLocalDB(userId, req);

        System.out.println("User created: " + userId);

        return new SignUpAuthResponse(true, message, userId, accessToken);
    }

    /**
     * Sign in an existing user
     */
    public SignInAuthResponse signIn(SignInAuthRequest req) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        // Call Supabase
        JSONObject json = callSupabaseAuth(url, req.getEmail(), req.getPassword());

        // Extract user data (might be nested or at root)
        String userId = extractUserId(json);
        String accessToken = extractAccessToken(json);

        if (accessToken == null || accessToken.isEmpty()) {
            throw new RuntimeException("Failed to extract access token. Please confirm your email first.");
        }

        System.out.println("User signed in: " + userId);

        return new SignInAuthResponse(true, "Sign-in successful", userId, accessToken);
    }

    /** ---------- Helper methods ---------- */

    /**
     * Call Supabase auth endpoint with email and password
     * Handles the HTTP request for both signup and signin
     */
    private JSONObject callSupabaseAuth(String url, String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);

        Map<String, String> body = Map.of(
                "email", email,
                "password", password
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return parseSupabaseResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to Supabase: " + e.getMessage());
        }
    }

    /**
     * Parse and validate Supabase response
     */
    private JSONObject parseSupabaseResponse(ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Supabase call failed: " + response.getStatusCode());
        }

        try {
            return new JSONObject(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Supabase response: " + e.getMessage());
        }
    }

    /**
     * Extract user ID from Supabase response
     * Handles both nested "user" object and root level
     */
    private String extractUserId(JSONObject json) {
        // Try nested "user" object first
        JSONObject userJson = json.optJSONObject("user");
        if (userJson != null) {
            return userJson.optString("id", null);
        }
        // Fallback to root level
        return json.optString("id", null);
    }

    /**
     * Extract access token from Supabase response
     * Handles both direct "access_token" and nested "session.access_token"
     */
    private String extractAccessToken(JSONObject json) {
        if (json.has("access_token")) {
            return json.optString("access_token");
        } else if (json.has("session")) {
            JSONObject session = json.optJSONObject("session");
            if (session != null) {
                return session.optString("access_token");
            }
        }
        return null;
    }

    /**
     * Save user details to local database
     */
    private void saveUserToLocalDB(String userId, SignUpAuthRequest req) {
        try {
            UserDetails user = new UserDetails();
            user.setUser_id(UUID.fromString(userId));
            user.setPhonenumber(req.getPhoneNumber());
            user.setDateofbirth(req.getDateOfBirth());
            userDetailsRepo.save(user);

            System.out.println("User details saved to local database");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user to database: " + e.getMessage());
        }
    }
}