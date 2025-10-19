package com.example.cube.service;

import java.util.UUID;

public interface StripeConnectService {

    /**
     * Create Connected Account and return onboarding link
     * @param userId User requesting payout capability
     * @return Stripe onboarding URL
     */
    String createConnectedAccountAndGetOnboardingLink(UUID userId);

    /**
     * Check and update onboarding status from Stripe
     * @param accountId Stripe account ID
     */
    void updateAccountStatus(String accountId);
}