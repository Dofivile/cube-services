package com.example.cube.service;

import java.util.UUID;

/**
 * Service for Stripe Connect embedded onboarding using Account Sessions.
 * This is the modern approach that renders natively in mobile apps without webviews.
 */
public interface StripeEmbeddedOnboardingService {

    /**
     * Create Connected Account and return Account Session client secret for embedded onboarding
     * @param userId User requesting payout capability
     * @return Account Session client secret to be used with Stripe's embedded component SDK
     */
    String createConnectedAccountAndGetAccountSession(UUID userId);

    /**
     * Check and update onboarding status from Stripe
     * @param accountId Stripe account ID
     */
    void updateAccountStatus(String accountId);
}

