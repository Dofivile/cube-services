package com.example.cube.service;

import com.example.cube.dto.request.PaymentRequestDTO;
import com.example.cube.dto.response.PaymentResponseDTO;

public interface PaymentService {

    /**
     * Process a member's payment for a cycle
     * Records payment transaction and deposits to bank
     *
     * @param request Payment request containing userId, cubeId, memberId, cycleNumber
     * @return Payment response with success status
     */
    PaymentResponseDTO processPayment(PaymentRequestDTO request);
}