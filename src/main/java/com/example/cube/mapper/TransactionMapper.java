package com.example.cube.mapper;

import com.example.cube.dto.response.TransactionDTO;
import com.example.cube.model.Cube;
import com.example.cube.model.Transaction;
import com.example.cube.repository.CubeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles conversions between Transaction entities and DTOs
 */
@Component
public class TransactionMapper {

    @Autowired
    private CubeRepository cubeRepository;

    /**
     * Convert Transaction entity to DTO with enriched information
     */
    public TransactionDTO toDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();

        // Basic fields
        dto.setPaymentId(transaction.getPaymentId());
        dto.setUserId(transaction.getUserId());
        dto.setCubeId(transaction.getCubeId());
        dto.setAmount(transaction.getAmount());
        dto.setCycleNumber(transaction.getCycleNumber());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setProcessedAt(transaction.getProcessedAt());
        dto.setStripePaymentIntentId(transaction.getStripePaymentIntentId());
        dto.setStripeTransferId(transaction.getStripeTransferId());
        dto.setFailureReason(transaction.getFailureReason());

        // Convert type ID to human-readable name
        dto.setType(getTransactionType(transaction.getTypeId()));

        // Convert status ID to human-readable name
        dto.setStatus(getTransactionStatus(transaction.getStatusId()));

        // Get cube name and currency
        Cube cube = cubeRepository.findById(transaction.getCubeId()).orElse(null);
        dto.setCubeName(cube != null ? cube.getName() : "Unknown Cube");
        dto.setCurrency(cube != null ? cube.getCurrency() : "USD");

        return dto;
    }

    /**
     * Convert type ID to readable string
     */
    private String getTransactionType(Integer typeId) {
        if (typeId == null) return "UNKNOWN";
        switch (typeId) {
            case 1: return "PAYMENT";
            case 2: return "PAYOUT";
            default: return "UNKNOWN";
        }
    }

    /**
     * Convert status ID to readable string
     */
    private String getTransactionStatus(Integer statusId) {
        if (statusId == null) return "UNKNOWN";
        switch (statusId) {
            case 1: return "PENDING";
            case 2: return "COMPLETED";
            case 3: return "FAILED";
            default: return "UNKNOWN";
        }
    }
}