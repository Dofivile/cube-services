package com.example.cube.service;

import com.example.cube.model.Bank;
import java.math.BigDecimal;

public interface BankService {

    // Add money to bank (when members contribute)
    void deposit(BigDecimal amount);

    // Remove money from bank (when winner gets paid)
    void withdraw(BigDecimal amount);

    // Get current bank balance
    BigDecimal getBalance();

    // Get the bank entity
    Bank getBank();
}