package com.example.cube.service;

import com.example.cube.dto.response.PaymentIntentResponse;
import java.util.UUID;

public interface StripeCardPaymentService {
    PaymentIntentResponse createCardPaymentIntent(UUID userId, UUID cubeId, UUID memberId, Integer cycleNumber);
    void handleCardPaymentIntentSucceeded(String paymentIntentId);
}