package com.example.cube.service;

import com.example.cube.dto.request.CreateFundingSourceRequest;
import com.example.cube.dto.request.VerifyMicroDepositsRequest;
import com.example.cube.dto.response.FundingSourceResponse;

import java.util.List;
import java.util.UUID;

public interface DwollaFundingSourceService {

    /**
     * Add a bank account to a customer (unverified initially)
     */
    FundingSourceResponse createFundingSource(UUID userId, CreateFundingSourceRequest request);

    /**
     * List all funding sources for a customer
     */
    List<FundingSourceResponse> listFundingSources(UUID userId);

    /**
     * Get details of a specific funding source
     */
    FundingSourceResponse getFundingSource(String fundingSourceId);

    /**
     * Initiate micro-deposit verification
     */
    FundingSourceResponse initiateMicroDeposits(UUID userId, String fundingSourceId);

    /**
     * Verify micro-deposits
     */
    FundingSourceResponse verifyMicroDeposits(UUID userId, String fundingSourceId, VerifyMicroDepositsRequest request);

    /**
     * Remove a funding source
     */
    void removeFundingSource(UUID userId, String fundingSourceId);

    /**
     * Get the primary verified funding source for a user (for transfers)
     */
    FundingSourceResponse getPrimaryVerifiedFundingSource(UUID userId);
}