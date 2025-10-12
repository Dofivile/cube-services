package com.example.cube.service.supabass;

import com.example.cube.dto.request.AuthRequestDTO;
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

    public void signUp(AuthRequestDTO req) {
        String url = supabaseUrl + "/auth/v1/signup";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);

        Map<String, String> body = Map.of(
                "email", req.getEmail(),
                "password", req.getPassword()
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject json = new JSONObject(response.getBody());
            String userId = json.optString("id", null);
            String email = json.optString("email", null);
            System.out.println("User created: " + userId + " | " + email);

            UserDetails user = new UserDetails();
            user.setUser_id(UUID.fromString(userId));
            user.setPhonenumber(req.getPhoneNumber());
            user.setDateofbirth(req.getDateOfBirth());
            userDetailsRepo.save(user);
        }
    }

    public ResponseEntity<String> signIn(AuthRequestDTO req) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);

        Map<String, String> body = Map.of(
                "email", req.getEmail(),
                "password", req.getPassword()
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject json = new JSONObject(response.getBody());
            String accessToken = json.optString("access_token", null);
            String userId = json.getJSONObject("user").optString("id", null);
            System.out.println("User signed in: " + userId);
            return ResponseEntity.ok(accessToken);
        } else {
            return ResponseEntity.status(response.getStatusCode()).body("User not found or invalid credentials");
        }
    }

}


