package com.example.cube.service;

import com.example.cube.dto.response.PaymentIntentResponse;
import java.util.UUID;

public interface StripePaymentService {
    PaymentIntentResponse createPaymentIntent(UUID userId, UUID cubeId, UUID memberId, Integer cycleNumber);
    void handlePaymentIntentSucceeded(String paymentIntentId);
}