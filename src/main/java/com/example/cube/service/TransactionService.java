package com.example.cube.service;

import com.example.cube.dto.response.TransactionHistoryResponse;
import java.util.UUID;

/**
 * Service interface for transaction-related operations
 */
public interface TransactionService {

    /**
     * Get complete transaction history for a user
     * Includes all payments made and payouts received
     *
     * @param userId - User ID
     * @return TransactionHistoryResponse with all user transactions and totals
     */
    TransactionHistoryResponse getUserTransactionHistory(UUID userId);
}