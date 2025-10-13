package com.example.cube.controller;

import com.example.cube.dto.request.AuthRequestDTO;
import com.example.cube.service.supabass.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAuthService userAuthService;

    @Autowired
    public AuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Boolean>> signup(@Valid @RequestBody AuthRequestDTO req) {
        userAuthService.signUp(req);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, String>> signIn(@RequestBody AuthRequestDTO req) {
        ResponseEntity<String> response = userAuthService.signIn(req);
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(Map.of(
                    "success", "true",
                    "message", "Sign-in successful",
                    "token", response.getBody()
            ));
        }
        return ResponseEntity.status(response.getStatusCode()).body(Map.of(
                "success", "false",
                "message", "Invalid credentials"
        ));
    }
}
