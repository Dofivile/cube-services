package com.example.cube.service.impl;

import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.StripeEmbeddedOnboardingService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountSession;
import com.stripe.model.Customer;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountSessionCreateParams;
import com.stripe.param.CustomerCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of Stripe Connect embedded onboarding using Account Sessions.
 * This creates native mobile experiences without webviews.
 */
@Service
public class StripeEmbeddedOnboardingServiceImpl implements StripeEmbeddedOnboardingService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional
    public String createConnectedAccountAndGetAccountSession(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create Stripe connected account if it doesn't exist
        if (user.getStripeAccountId() == null) {
            try {
                System.out.println("📝 Creating new Stripe Custom account for embedded onboarding (user: " + userId + ")");

                AccountCreateParams params = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.CUSTOM)  // Custom account for embedded onboarding without webview
                        .setCountry("US")
                        .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                        // Controller settings for Custom accounts - platform handles requirements
                        .setController(AccountCreateParams.Controller.builder()
                                .setFees(AccountCreateParams.Controller.Fees.builder()
                                        .setPayer(AccountCreateParams.Controller.Fees.Payer.APPLICATION)
                                        .build())
                                .setLosses(AccountCreateParams.Controller.Losses.builder()
                                        .setPayments(AccountCreateParams.Controller.Losses.Payments.APPLICATION)
                                        .build())
                                .setRequirementCollection(AccountCreateParams.Controller.RequirementCollection.APPLICATION)
                                .setStripeDashboard(AccountCreateParams.Controller.StripeDashboard.builder()
                                        .setType(AccountCreateParams.Controller.StripeDashboard.Type.NONE)
                                        .build())
                                .build())
                        // Enable required capabilities
                        .setCapabilities(AccountCreateParams.Capabilities.builder()
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                        .setRequested(true)
                                        .build())
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                        .setRequested(true)
                                        .build())
                                .build())
                        .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                                .setMcc("6012") // Financial institutions
                                .setProductDescription("Personal savings pool participant")
                                .build())
                        .setSettings(AccountCreateParams.Settings.builder()
                                .setPayouts(AccountCreateParams.Settings.Payouts.builder()
                                        .setSchedule(AccountCreateParams.Settings.Payouts.Schedule.builder()
                                                .setInterval(AccountCreateParams.Settings.Payouts.Schedule.Interval.MANUAL)
                                                .build())
                                        .build())
                                .build())
                        .putMetadata("user_id", userId.toString())
                        .build();

                Account account = Account.create(params);
                user.setStripeAccountId(account.getId());

                // Create Stripe customer if needed
                if (user.getStripeCustomerId() == null) {
                    CustomerCreateParams customerParams = CustomerCreateParams.builder()
                            .putMetadata("user_id", userId.toString())
                            .build();
                    Customer customer = Customer.create(customerParams);
                    user.setStripeCustomerId(customer.getId());
                    System.out.println("✅ Created Stripe customer: " + customer.getId());
                }

                userDetailsRepository.save(user);
                System.out.println("✅ Created Stripe Connect account: " + account.getId());

            } catch (StripeException e) {
                System.err.println("❌ Failed to create Stripe account: " + e.getMessage());
                throw new RuntimeException("Failed to create Stripe account: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ User already has Stripe account: " + user.getStripeAccountId());
        }

        // Create and return Account Session client secret
        return createAccountSession(user.getStripeAccountId());
    }

    @Override
    @Transactional
    public void updateAccountStatus(String accountId) {
        try {
            Account account = Account.retrieve(accountId);

            Optional<UserDetails> userOpt = userDetailsRepository.findByStripeAccountId(accountId);
            if (userOpt.isEmpty()) {
                System.err.println("❌ No user found with stripe_account_id: " + accountId);
                return;
            }

            UserDetails user = userOpt.get();
            Boolean payoutsEnabled = account.getPayoutsEnabled();

            user.setStripePayoutsEnabled(payoutsEnabled);
            userDetailsRepository.save(user);

            System.out.println("✅ Updated payout status for user " + user.getUser_id()
                    + ": payoutsEnabled=" + payoutsEnabled);

        } catch (StripeException e) {
            System.err.println("❌ Failed to retrieve account status: " + e.getMessage());
        }
    }

    /**
     * Creates an Account Session for the embedded onboarding component.
     * The client secret is used by mobile SDKs to render the onboarding flow natively.
     */
    private String createAccountSession(String accountId) {
        try {
            System.out.println("🔐 Creating Account Session for account: " + accountId);

            AccountSessionCreateParams params = AccountSessionCreateParams.builder()
                    .setAccount(accountId)
                    .setComponents(
                            AccountSessionCreateParams.Components.builder()
                                    .setAccountOnboarding(
                                            AccountSessionCreateParams.Components.AccountOnboarding.builder()
                                                    .setEnabled(true)
                                                    .setFeatures(
                                                            AccountSessionCreateParams.Components.AccountOnboarding.Features.builder()
                                                                    // Disable Stripe user authentication for native embedded experience
                                                                    // This removes the webview requirement for Custom accounts
                                                                    .setDisableStripeUserAuthentication(true)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            AccountSession session = AccountSession.create(params);
            System.out.println("✅ Generated Account Session client secret");
            return session.getClientSecret();

        } catch (StripeException e) {
            System.err.println("❌ Failed to create Account Session: " + e.getMessage());
            throw new RuntimeException("Failed to create Account Session: " + e.getMessage());
        }
    }
}

