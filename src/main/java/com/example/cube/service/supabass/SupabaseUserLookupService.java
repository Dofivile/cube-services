package com.example.cube.service.supabass;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class SupabaseUserLookupService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get Supabase user ID by email
     */
    public String getUserIdByEmail(String email) {
        String url = supabaseUrl + "/auth/v1/admin/users?email=" + email;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject obj = new JSONObject(response.getBody());
            JSONArray users = obj.optJSONArray("users");
            if (users != null && !users.isEmpty()) {
                return users.getJSONObject(0).getString("id");
            }
        }
        return null;
    }

    /**
     * Get email by Supabase user ID (reverse lookup)
     */
    public String getEmailByUserId(String userId) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject user = new JSONObject(response.getBody());
                return user.optString("email", null);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to lookup email for user " + userId + ": " + e.getMessage());
        }

        return null;
    }
}