package com.example.cube.controller;

import com.example.cube.dto.request.PaymentRequestDTO;
import com.example.cube.dto.response.PaymentResponse;
import com.example.cube.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments
     * Process a member's payment for a cycle
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}