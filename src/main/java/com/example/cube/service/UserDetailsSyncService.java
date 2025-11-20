package com.example.cube.service;

import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Ensures we always have a user_details record for any Supabase user,
 * regardless of how they authenticated (email/password, Google, Apple, etc.).
 */
@Service
public class UserDetailsSyncService {

    private final UserDetailsRepository userDetailsRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public UserDetailsSyncService(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    @Transactional
    public UserDetails ensureUserDetails(UUID userId) {
        Optional<UserDetails> existing = userDetailsRepository.findById(userId);
        if (existing.isPresent() && hasBasicProfile(existing.get())) {
            return existing.get();
        }

        UserDetails entity = existing.orElseGet(() -> {
            UserDetails details = new UserDetails();
            details.setUser_id(userId);
            return details;
        });

        JSONObject profile = fetchSupabaseUser(userId);
        applyNamesFromProfile(entity, profile);

        return userDetailsRepository.save(entity);
    }

    private boolean hasBasicProfile(UserDetails details) {
        return details.getFirstName() != null && !details.getFirstName().isBlank()
                && details.getLastName() != null && !details.getLastName().isBlank();
    }

    private JSONObject fetchSupabaseUser(UUID userId) {
        String url = supabaseUrl + "/auth/v1/admin/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new JSONObject(response.getBody());
            }
        } catch (RestClientException e) {
            System.err.println("❌ Failed to fetch Supabase user " + userId + ": " + e.getMessage());
        }
        return null;
    }

    private void applyNamesFromProfile(UserDetails details, JSONObject profile) {
        if (profile == null) {
            // Ensure at least minimal record exists
            if (details.getFirstName() == null) {
                details.setFirstName("Cube");
            }
            if (details.getLastName() == null) {
                details.setLastName("Member");
            }
            return;
        }

        JSONObject metadata = firstNonEmpty(
                profile.optJSONObject("user_metadata"),
                profile.optJSONObject("raw_user_meta_data")
        );

        String firstName = metadata != null ? metadata.optString("first_name", null) : null;
        String lastName = metadata != null ? metadata.optString("last_name", null) : null;

        if ((firstName == null || firstName.isBlank()) && metadata != null) {
            String fullName = metadata.optString("full_name", null);
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                firstName = parts[0];
                if (parts.length > 1) {
                    lastName = parts[1];
                }
            }
        }

        if ((firstName == null || firstName.isBlank()) && profile.has("email")) {
            String email = profile.optString("email");
            if (email != null && !email.isBlank()) {
                firstName = email.split("@")[0];
            }
        }

        if (details.getFirstName() == null || details.getFirstName().isBlank()) {
            details.setFirstName(firstName != null ? firstName : "Cube");
        }

        if (details.getLastName() == null || details.getLastName().isBlank()) {
            details.setLastName(lastName != null ? lastName : "Member");
        }
    }

    private JSONObject firstNonEmpty(JSONObject primary, JSONObject fallback) {
        if (primary != null && primary.length() > 0) {
            return primary;
        }
        if (fallback != null && fallback.length() > 0) {
            return fallback;
        }
        return null;
    }
}
