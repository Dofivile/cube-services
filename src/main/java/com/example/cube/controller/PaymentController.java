package com.example.cube.controller;

import com.example.cube.dto.request.CreatePaymentIntentRequest;
import com.example.cube.dto.response.PaymentIntentResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.StripePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreatePaymentIntentRequest request) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        PaymentIntentResponse response = stripePaymentService.createPaymentIntent(
                userId,
                request.getCubeId(),
                request.getMemberId(),
                request.getCycleNumber()
        );

        return ResponseEntity.ok(response);
    }
}