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
    public String createConnectedAccountAndGetOnboardingLink(UUID userId, String returnUrl, String refreshUrl) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStripeAccountId() != null) {
            System.out.println("User already has Stripe account: " + user.getStripeAccountId());
            return generateAccountLink(user.getStripeAccountId(), returnUrl, refreshUrl);
        }

        try {
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("US")
                    .setBusinessType(AccountCreateParams.BusinessType.INDIVIDUAL)

                    // ✅ FULL CAPABILITIES (Transfers + Card Payments)
                    .setCapabilities(AccountCreateParams.Capabilities.builder()
                            .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                    .setRequested(true)
                                    .build())
                            .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                    .setRequested(true)
                                    .build())
                            .build())

                    .setBusinessProfile(AccountCreateParams.BusinessProfile.builder()
                            .setMcc("6012")
                            .setProductDescription("Personal savings pool participant")
                            .setUrl(null)
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

            if (user.getStripeCustomerId() == null) {
                CustomerCreateParams customerParams = CustomerCreateParams.builder()
                        .putMetadata("user_id", userId.toString())
                        .build();

                Customer customer = Customer.create(customerParams);
                user.setStripeCustomerId(customer.getId());
                System.out.println("Created Stripe customer: " + customer.getId());
            }

            userDetailsRepository.save(user);

            System.out.println("Created Stripe Connect account: " + account.getId());

            return generateAccountLink(account.getId(), returnUrl, refreshUrl);

        } catch (StripeException e) {
            System.err.println("Failed to create Stripe account: " + e.getMessage());
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
                System.err.println("No user found with stripe_account_id: " + accountId);
                return;
            }

            UserDetails user = userOpt.get();
            Boolean payoutsEnabled = account.getPayoutsEnabled();

            user.setStripePayoutsEnabled(payoutsEnabled);
            userDetailsRepository.save(user);

            System.out.println("Updated payout status for user " + user.getUserId()
                    + ": payoutsEnabled=" + payoutsEnabled);

        } catch (StripeException e) {
            System.err.println("Failed to retrieve account status: " + e.getMessage());
        }
    }

    private String generateAccountLink(String accountId, String returnUrl, String refreshUrl) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl(refreshUrl)
                    .setReturnUrl(returnUrl)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .setCollect(AccountLinkCreateParams.Collect.EVENTUALLY_DUE)
                    .build();

            AccountLink accountLink = AccountLink.create(params);
            System.out.println("Generated onboarding link for account: " + accountId);
            System.out.println("Return URL: " + returnUrl);
            System.out.println("Refresh URL: " + refreshUrl);
            return accountLink.getUrl();

        } catch (StripeException e) {
            System.err.println("Failed to generate account link: " + e.getMessage());
            throw new RuntimeException("Failed to generate onboarding link: " + e.getMessage());
        }
    }
}
