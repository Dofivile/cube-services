package com.example.cube.service.impl;

import com.example.cube.model.Bank;
import com.example.cube.repository.BankRepository;
import com.example.cube.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;

    @Autowired
    public BankServiceImpl(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @Override
    @Transactional
    public void deposit(BigDecimal amount) {
        Bank bank = getBank();
        bank.setAmount(bank.getAmount().add(amount));
        bankRepository.save(bank);
    }

    @Override
    @Transactional
    public void withdraw(BigDecimal amount) {
        Bank bank = getBank();

        // Check sufficient funds
        if (bank.getAmount().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in bank. Balance: " +
                    bank.getAmount() + ", Required: " + amount);
        }

        bank.setAmount(bank.getAmount().subtract(amount));
        bankRepository.save(bank);
    }

    @Override
    public BigDecimal getBalance() {
        return getBank().getAmount();
    }

    @Override
    public Bank getBank() {
        // Assuming bank_id = 1 is the main bank
        return bankRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Bank not found"));
    }
}