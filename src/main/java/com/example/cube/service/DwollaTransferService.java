package com.example.cube.service;

import com.example.cube.dto.request.CreateTransferRequest;
import com.example.cube.dto.request.PayoutRequest;
import com.example.cube.dto.response.TransferResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DwollaTransferService {

    /**
     * User contributes to cube (User Bank → Platform Balance)
     */
    TransferResponse createCubeContribution(UUID userId, UUID cubeId, UUID memberId, Integer cycleNumber);

    /**
     * Send payout to winner (Platform Balance → Winner Bank)
     */
    TransferResponse sendPayoutToWinner(UUID winnerId, BigDecimal amount, UUID cubeId, Integer cycleNumber);

    /**
     * Get transfer status
     */
    TransferResponse getTransferStatus(String transferId);

    /**
     * List all transfers for a customer
     */
    List<TransferResponse> listCustomerTransfers(UUID userId);

    /**
     * Cancel a pending transfer
     */
    void cancelTransfer(String transferId);

    /**
     * Handle transfer webhook events (processed, failed, cancelled)
     */
    void handleTransferWebhook(String transferId, String status);
}