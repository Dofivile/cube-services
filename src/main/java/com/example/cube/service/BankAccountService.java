// New file: BankAccountService.java
package com.example.cube.service;

import java.util.UUID;

public interface BankAccountService {
    String createSetupIntentForBankAccount(UUID userId);
    void saveBankAccountDetails(UUID userId, String paymentMethodId);
    boolean userHasBankAccountLinked(UUID userId);
}