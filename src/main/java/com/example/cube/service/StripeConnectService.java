package com.example.cube.service;

import java.util.UUID;

public interface StripeConnectService {

    /**
     * Create Connected Account and return onboarding link
     * @param userId User requesting payout capability
     * @param returnUrl URL to redirect after successful onboarding
     * @param refreshUrl URL to redirect if onboarding needs to be restarted
     * @return Stripe onboarding URL
     */
    String createConnectedAccountAndGetOnboardingLink(UUID userId, String returnUrl, String refreshUrl);

    /**
     * Check and update onboarding status from Stripe
     * @param accountId Stripe account ID
     */
    void updateAccountStatus(String accountId);
}