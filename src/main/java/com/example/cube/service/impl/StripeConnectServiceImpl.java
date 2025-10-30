package com.example.cube.service.impl;

import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.StripeConnectService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Customer;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.CustomerCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class StripeConnectServiceImpl implements StripeConnectService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${cube.frontend.url}")
    private String frontendUrl;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional
    public String createConnectedAccountAndGetOnboardingLink(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If user already has an account, just generate a new link
        if (user.getStripeAccountId() != null) {
            System.out.println("User already has Stripe account: " + user.getStripeAccountId());
            return generateAccountLink(user.getStripeAccountId());
        }

        try {
            // Create Express Connected Account for receiving payouts
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("US")
                    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)
                    .setCapabilities(AccountCreateParams.Capabilities.builder()
                            .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                    .setRequested(true)
                                    .build())
                            .build())
                    .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                            .setMcc("6012")  // Financial institution - member-owned
                            .setProductDescription("Personal savings pool participant")
                            .setUrl(null)  // No website required
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

            // Save account ID to database
            user.setStripeAccountId(account.getId());

            // ✨ Also create a Stripe Customer for making payments
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

            // Generate onboarding link
            return generateAccountLink(account.getId());

        } catch (StripeException e) {
            System.err.println("❌ Failed to create Stripe account: " + e.getMessage());
            throw new RuntimeException("Failed to create Stripe account: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateAccountStatus(String accountId) {
        try {
            Account account = Account.retrieve(accountId);

            Optional<UserDetails> userOpt = userDetailsRepository.findByStripeAccountId(accountId);
            if (userOpt.isEmpty()) {
                System.err.println("⚠️ No user found with stripe_account_id: " + accountId);
                return;
            }

            UserDetails user = userOpt.get();

            // Check if payouts are enabled (only thing we care about)
            Boolean payoutsEnabled = account.getPayoutsEnabled();

            // Update payout status
            user.setStripePayoutsEnabled(payoutsEnabled);
            userDetailsRepository.save(user);

            System.out.println("✅ Updated payout status for user " + user.getUser_id() + ": payoutsEnabled=" + payoutsEnabled);

        } catch (StripeException e) {
            System.err.println("❌ Failed to retrieve account status: " + e.getMessage());
        }
    }

    private String generateAccountLink(String accountId) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl(frontendUrl + "/onboarding/refresh")
                    .setReturnUrl(frontendUrl + "/onboarding/complete")
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .setCollect(AccountLinkCreateParams.Collect.EVENTUALLY_DUE)
                    .build();

            AccountLink accountLink = AccountLink.create(params);

            System.out.println("✅ Generated onboarding link for account: " + accountId);
            return accountLink.getUrl();

        } catch (StripeException e) {
            System.err.println("❌ Failed to generate account link: " + e.getMessage());
            throw new RuntimeException("Failed to generate onboarding link: " + e.getMessage());
        }
    }
}