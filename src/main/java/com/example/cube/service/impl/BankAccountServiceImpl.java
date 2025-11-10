// BankAccountServiceImpl.java
package com.example.cube.service.impl;

import com.example.cube.dto.response.BankAccountStatusResponse;
import com.example.cube.model.UserDetails;
import com.example.cube.model.UserPaymentMethod;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.repository.UserPaymentMethodRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private UserPaymentMethodRepository userPaymentMethodRepository;

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
    // Retrieve the payment method
    PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
    
    // ✅ Get the SetupIntent to retrieve the mandate
    String setupIntentId = null;
    try {
        // The payment method should have a reference to the SetupIntent
        // We need to retrieve the SetupIntent that created this payment method
        com.stripe.param.SetupIntentListParams setupParams = 
            com.stripe.param.SetupIntentListParams.builder()
                .setPaymentMethod(paymentMethodId)
                .setLimit(1L)
                .build();
        
        com.stripe.model.SetupIntentCollection setupIntents = 
            com.stripe.model.SetupIntent.list(setupParams);
        
        if (!setupIntents.getData().isEmpty()) {
            SetupIntent setupIntent = setupIntents.getData().get(0);
            setupIntentId = setupIntent.getMandate();
            System.out.println("✅ Retrieved mandate: " + setupIntentId);
        }
    } catch (StripeException e) {
        System.err.println("⚠️ Could not retrieve SetupIntent/Mandate: " + e.getMessage());
    }
    
    // Attach payment method to customer
    paymentMethod.attach(
            PaymentMethodAttachParams.builder()
                    .setCustomer(user.getStripeCustomerId())
                    .build()
    );

    // Set as default payment method on Stripe customer
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

    // Get Financial Connections account ID and bank details
    String fcAccountId = paymentMethod.getUsBankAccount().getFinancialConnectionsAccount();
    String bankName = paymentMethod.getUsBankAccount().getBankName();
    String last4 = paymentMethod.getUsBankAccount().getLast4();

    // ✅ Check if this payment method already exists
    Optional<UserPaymentMethod> existingMethod = 
            userPaymentMethodRepository.findByStripePaymentMethodId(paymentMethodId);

    UserPaymentMethod userPaymentMethod;
    if (existingMethod.isPresent()) {
        // Update existing record
        userPaymentMethod = existingMethod.get();
        System.out.println("📝 Updating existing payment method: " + paymentMethodId);
    } else {
        // Create new record
        userPaymentMethod = new UserPaymentMethod();
        userPaymentMethod.setUserId(userId);
        userPaymentMethod.setStripePaymentMethodId(paymentMethodId);
        System.out.println("✨ Creating new payment method record");
    }

    // Set/update fields
    userPaymentMethod.setFinancialConnectionsAccountId(fcAccountId);
    userPaymentMethod.setBankName(bankName);
    userPaymentMethod.setLast4(last4);
    userPaymentMethod.setBankAccountVerified(true);
    userPaymentMethod.setMandateId(setupIntentId);  // ✅ SAVE THE MANDATE

    // ✅ Set as default if this is the user's first payment method
    List<UserPaymentMethod> userMethods = userPaymentMethodRepository.findByUserId(userId);
    if (userMethods.isEmpty()) {
        userPaymentMethod.setIsDefault(true);
        System.out.println("⭐ Setting as default (first payment method)");
    } else {
        // If no default exists, make this one default
        Optional<UserPaymentMethod> currentDefault = 
                userPaymentMethodRepository.findByUserIdAndIsDefaultTrue(userId);
        if (currentDefault.isEmpty() && !existingMethod.isPresent()) {
            userPaymentMethod.setIsDefault(true);
            System.out.println("⭐ Setting as default (no default exists)");
        }
    }

    userPaymentMethodRepository.save(userPaymentMethod);

    System.out.println("✅ Bank account linked for user: " + userId);
    System.out.println("   Payment Method: " + paymentMethodId);
    System.out.println("   Mandate: " + setupIntentId);
    System.out.println("   Bank: " + bankName + " ****" + last4);
    System.out.println("   Is Default: " + userPaymentMethod.getIsDefault());

} catch (StripeException e) {
    throw new RuntimeException("Failed to retrieve payment method: " + e.getMessage());
}
    }

    @Override
    public BankAccountStatusResponse getBankAccountStatus(UUID userId) {
        // Check if user has any verified bank account
        Optional<UserPaymentMethod> defaultMethod =
                userPaymentMethodRepository.findByUserIdAndIsDefaultTrue(userId);

        if (defaultMethod.isPresent() && defaultMethod.get().getBankAccountVerified()) {
            UserPaymentMethod method = defaultMethod.get();
            return new BankAccountStatusResponse(
                    true,
                    method.getBankName(),
                    method.getLast4(),
                    method.getIsDefault(),
                    method.getBankAccountVerified(),
                    method.getStripePaymentMethodId()
            );
        }

        // If no default, check for any verified account
        List<UserPaymentMethod> userMethods = userPaymentMethodRepository.findByUserId(userId);
        Optional<UserPaymentMethod> anyVerified = userMethods.stream()
                .filter(m -> m.getBankAccountVerified() != null && m.getBankAccountVerified())
                .findFirst();

        if (anyVerified.isPresent()) {
            UserPaymentMethod method = anyVerified.get();
            return new BankAccountStatusResponse(
                    true,
                    method.getBankName(),
                    method.getLast4(),
                    method.getIsDefault(),
                    method.getBankAccountVerified(),
                    method.getStripePaymentMethodId()
            );
        }

        // No bank account linked
        return new BankAccountStatusResponse(false, null, null, null, null, null);
    }

    @Override
    public boolean userHasBankAccountLinked(UUID userId) {
        return userPaymentMethodRepository.existsByUserIdAndBankAccountVerifiedTrue(userId);
    }

    @Override
    @Transactional
    public void deleteBankAccount(UUID userId, String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new IllegalArgumentException("Payment method ID is required");
        }

        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPaymentMethod method = userPaymentMethodRepository
                .findByUserIdAndStripePaymentMethodId(userId, paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found for user"));

        try {
            PaymentMethod stripePaymentMethod = PaymentMethod.retrieve(paymentMethodId);

            if (stripePaymentMethod.getCustomer() != null &&
                    !stripePaymentMethod.getCustomer().equals(user.getStripeCustomerId())) {
                throw new RuntimeException("Payment method does not belong to this user");
            }

            stripePaymentMethod.detach();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to detach payment method: " + e.getMessage());
        }

        boolean wasDefault = method.getIsDefault() != null && method.getIsDefault();
        userPaymentMethodRepository.delete(method);

        if (wasDefault) {
            Optional<UserPaymentMethod> nextDefault =
                    userPaymentMethodRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

            if (nextDefault.isPresent()) {
                UserPaymentMethod newDefault = nextDefault.get();
                newDefault.setIsDefault(true);
                userPaymentMethodRepository.save(newDefault);
                updateStripeCustomerDefaultPaymentMethod(user.getStripeCustomerId(), newDefault.getStripePaymentMethodId());
            } else {
                // No methods left, clear Stripe default
                updateStripeCustomerDefaultPaymentMethod(user.getStripeCustomerId(), null);
            }
        }
    }

    private void updateStripeCustomerDefaultPaymentMethod(String customerId, String paymentMethodId) {
        try {
            Customer customer = Customer.retrieve(customerId);
            CustomerUpdateParams.InvoiceSettings.Builder invoiceSettingsBuilder =
                    CustomerUpdateParams.InvoiceSettings.builder();

            if (paymentMethodId == null) {
                invoiceSettingsBuilder.setDefaultPaymentMethod((String) null);
            } else {
                invoiceSettingsBuilder.setDefaultPaymentMethod(paymentMethodId);
            }

            CustomerUpdateParams params = CustomerUpdateParams.builder()
                    .setInvoiceSettings(invoiceSettingsBuilder.build())
                    .build();

            customer.update(params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to update Stripe customer default payment method: " + e.getMessage());
        }
    }
}
