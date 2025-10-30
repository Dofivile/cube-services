// New file: BankAccountServiceImpl.java
package com.example.cube.service.impl;

import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.BankAccountService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SetupIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public String createSetupIntentForBankAccount(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get or create Stripe customer
        String customerId = user.getStripeCustomerId();
        if (customerId == null) {
            throw new RuntimeException("User must have a Stripe customer ID");
        }

        try {
            SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                    .setCustomer(customerId)
                    .addPaymentMethodType("us_bank_account")
                    .setPaymentMethodOptions(
                            SetupIntentCreateParams.PaymentMethodOptions.builder()
                                    .setUsBankAccount(
                                            SetupIntentCreateParams.PaymentMethodOptions.UsBankAccount.builder()
                                                    .setFinancialConnections(
                                                            SetupIntentCreateParams.PaymentMethodOptions.UsBankAccount
                                                                    .FinancialConnections.builder()
                                                                    .addPermission(SetupIntentCreateParams.PaymentMethodOptions
                                                                            .UsBankAccount.FinancialConnections.Permission.PAYMENT_METHOD)
                                                                    .addPermission(SetupIntentCreateParams.PaymentMethodOptions
                                                                            .UsBankAccount.FinancialConnections.Permission.BALANCES)
                                                                    .addPermission(SetupIntentCreateParams.PaymentMethodOptions
                                                                            .UsBankAccount.FinancialConnections.Permission.OWNERSHIP)
                                                                    .build()
                                                    )
                                                    .setVerificationMethod(SetupIntentCreateParams.PaymentMethodOptions
                                                            .UsBankAccount.VerificationMethod.INSTANT)
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("user_id", userId.toString())
                    .build();

            SetupIntent setupIntent = SetupIntent.create(params);
            return setupIntent.getClientSecret();

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create setup intent: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void saveBankAccountDetails(UUID userId, String paymentMethodId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            paymentMethod.attach(
                    PaymentMethodAttachParams.builder()
                            .setCustomer(user.getStripeCustomerId())
                            .build()
            );

            Customer customer = Customer.retrieve(user.getStripeCustomerId());
            System.out.println("✅ customer: " + customer);
            CustomerUpdateParams params = CustomerUpdateParams.builder()
                    .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(paymentMethodId)
                                    .build()
                    )
                    .build();
            customer.update(params);

            // Get Financial Connections account ID
            String fcAccountId = paymentMethod.getUsBankAccount().getFinancialConnectionsAccount();

            // Save details
            user.setStripePaymentMethodId(paymentMethodId);
            user.setFinancialConnectionsAccountId(fcAccountId);
            user.setBankAccountVerified(true);

            userDetailsRepository.save(user);

            System.out.println("✅ Bank account linked for user: " + userId);
            System.out.println("   Payment Method: " + paymentMethodId);

        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve payment method: " + e.getMessage());
        }
    }

    @Override
    public boolean userHasBankAccountLinked(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId).orElse(null);
        return user != null &&
                user.getStripePaymentMethodId() != null &&
                user.getBankAccountVerified() != null &&
                user.getBankAccountVerified();
    }
}