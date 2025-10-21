package com.example.cube.controller;

import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.StripeConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payouts")
public class PayoutController {

    @Autowired
    private StripeConnectService stripeConnectService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    /**
     * Initiate Stripe Connect onboarding
     * Returns URL to redirect user to
     */
    @PostMapping("/initiate-onboarding")
    public ResponseEntity<Map<String, String>> initiateOnboarding(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        System.out.println("📝 Initiating onboarding for user: " + userId);

        String onboardingUrl = stripeConnectService.createConnectedAccountAndGetOnboardingLink(userId);

        Map<String, String> response = new HashMap<>();
        response.put("onboardingUrl", onboardingUrl);
        response.put("message", "Redirect user to this URL to complete onboarding");

        return ResponseEntity.ok(response);
    }

}