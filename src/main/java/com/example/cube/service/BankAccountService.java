// New file: BankAccountService.java
package com.example.cube.service;

import com.example.cube.dto.response.BankAccountStatusResponse;
import java.util.UUID;

public interface BankAccountService {
    String createSetupIntentForBankAccount(UUID userId);
    void saveBankAccountDetails(UUID userId, String paymentMethodId);
    boolean userHasBankAccountLinked(UUID userId);
    BankAccountStatusResponse getBankAccountStatus(UUID userId);
    void deleteBankAccount(UUID userId, String paymentMethodId);
}
