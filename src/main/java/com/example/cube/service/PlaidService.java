package com.example.cube.service;

import com.example.cube.dto.request.PlaidExchangeRequest;
import com.example.cube.dto.response.PlaidLinkResponse;

import java.util.UUID;

public interface PlaidService {

    /**
     * Exchange Plaid public token and create Dwolla funding source
     */
    PlaidLinkResponse exchangeTokenAndLinkToDwolla(UUID userId, PlaidExchangeRequest request);

    /**
     * Create Plaid Link token for frontend
     */
    String createLinkToken(UUID userId);
}