package com.example.cube.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PayoutService {

    /**
     * Send payout to a cube winner
     *
     * @param winnerId The user receiving the payout
     * @param amount The amount to send (in dollars)
     * @param cubeId The cube this payout is for
     * @param cycleNumber The cycle number
     * @return The payment transaction ID
     */
    UUID sendPayoutToWinner(UUID winnerId, BigDecimal amount, UUID cubeId, Integer cycleNumber);

    /**
     * Check if a user can receive payouts
     */
    boolean canUserReceivePayouts(UUID userId);
}