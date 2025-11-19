package com.example.cube.service.impl;

import com.example.cube.dto.response.TransactionDTO;
import com.example.cube.dto.response.TransactionHistoryResponse;
import com.example.cube.mapper.TransactionMapper;
import com.example.cube.model.Transaction;
import com.example.cube.repository.PaymentTransactionRepository;
import com.example.cube.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    @Cacheable(value = "transactions", key = "#userId")
    public TransactionHistoryResponse getUserTransactionHistory(UUID userId) {

        // Get all transactions for this user (ordered by most recent)
        List<Transaction> transactions = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        // Convert to DTOs using mapper
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());

        // Calculate total payments (completed only)
        BigDecimal totalPayments = transactions.stream()
                .filter(t -> t.getTypeId() == 1 && t.getStatusId() == 2) // PAYMENT + COMPLETED
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total payouts (completed only)
        BigDecimal totalPayouts = transactions.stream()
                .filter(t -> t.getTypeId() == 2 && t.getStatusId() == 2) // PAYOUT + COMPLETED
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TransactionHistoryResponse(
                transactionDTOs,
                totalPayments,
                totalPayouts,
                transactionDTOs.size()
        );
    }
}