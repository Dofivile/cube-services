package com.example.cube.controller;

import com.example.cube.dto.request.auth.SignInAuthRequest;
import com.example.cube.dto.request.auth.SignUpAuthRequest;
import com.example.cube.dto.response.auth.SignInAuthResponse;
import com.example.cube.dto.response.auth.SignUpAuthResponse;
import com.example.cube.service.supabass.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAuthService userAuthService;

    @Autowired
    public AuthController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpAuthResponse> signup(@Valid @RequestBody SignUpAuthRequest req) {
        SignUpAuthResponse response = userAuthService.signUp(req);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<SignInAuthResponse> signIn(@Valid @RequestBody SignInAuthRequest req) {
        SignInAuthResponse response = userAuthService.signIn(req);
        return ResponseEntity.ok(response);
    }
}
