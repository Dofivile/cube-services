package com.example.cube.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response for user transaction history
 */
public class TransactionHistoryResponse {

    private List<TransactionDTO> transactions;
    private BigDecimal totalPayments;
    private BigDecimal totalPayouts;
    private int totalCount;

    // Constructors
    public TransactionHistoryResponse() {}

    public TransactionHistoryResponse(List<TransactionDTO> transactions,
                                      BigDecimal totalPayments,
                                      BigDecimal totalPayouts,
                                      int totalCount) {
        this.transactions = transactions;
        this.totalPayments = totalPayments;
        this.totalPayouts = totalPayouts;
        this.totalCount = totalCount;
    }

    // Getters and Setters
    public List<TransactionDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionDTO> transactions) {
        this.transactions = transactions;
    }

    public BigDecimal getTotalPayments() { return totalPayments; }
    public void setTotalPayments(BigDecimal totalPayments) {
        this.totalPayments = totalPayments;
    }

    public BigDecimal getTotalPayouts() { return totalPayouts; }
    public void setTotalPayouts(BigDecimal totalPayouts) {
        this.totalPayouts = totalPayouts;
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}