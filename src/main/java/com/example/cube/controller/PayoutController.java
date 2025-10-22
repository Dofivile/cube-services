package com.example.cube.controller;

import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.PayoutService;
import com.example.cube.service.StripeConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payouts")
public class PayoutController {

    private final StripeConnectService stripeConnectService;
    private final AuthenticationService authenticationService;
    private final UserDetailsRepository userDetailsRepository;
    private final PayoutService payoutService;

    @Autowired
    public PayoutController(StripeConnectService stripeConnectService,
                            AuthenticationService authenticationService,
                            UserDetailsRepository userDetailsRepository,
                            PayoutService payoutService) {
        this.stripeConnectService = stripeConnectService;
        this.authenticationService = authenticationService;
        this.userDetailsRepository = userDetailsRepository;
        this.payoutService = payoutService;
    }

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

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendPayout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        UUID winnerId = UUID.fromString(payload.get("winnerId").toString());
        UUID cubeId = UUID.fromString(payload.get("cubeId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        Integer cycle = (Integer) payload.get("cycle");

        UUID payoutId = payoutService.sendPayoutToWinner(winnerId, amount, cubeId, cycle);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "payoutId", payoutId,
                "message", "Payout processed"
        ));
    }
}