package com.example.cube.service.impl;

import com.example.cube.dto.request.PlaidExchangeRequest;
import com.example.cube.dto.response.FundingSourceResponse;
import com.example.cube.dto.response.PlaidLinkResponse;
import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.DwollaFundingSourceService;
import com.example.cube.service.PlaidService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PlaidServiceImpl implements PlaidService {

    @Value("${PLAID_CLIENT_ID}")
    private String plaidClientId;

    @Value("${PLAID_SECRET}")
    private String plaidSecret;

    @Value("${PLAID_BASE_URL}")
    private String plaidBaseUrl;

    @Value("${PLAID_ENV}")
    private String plaidEnv;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private DwollaFundingSourceService dwollaFundingSourceService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public PlaidLinkResponse exchangeTokenAndLinkToDwolla(UUID userId, PlaidExchangeRequest request) {

        System.out.println("\n🔗 ========== PLAID → DWOLLA LINK ==========");
        System.out.println("User: " + userId);
        System.out.println("Public Token: " + request.getPublicToken().substring(0, 20) + "...");

        try {
            // STEP 1: Exchange public token for access token
            String accessToken = exchangePublicToken(request.getPublicToken());

            // STEP 2: Get bank account details from Plaid
            Map<String, Object> accountDetails = getAccountDetails(accessToken, request.getAccountId());

            // Extract account info
            String routingNumber = (String) accountDetails.get("routing");
            String accountNumber = (String) accountDetails.get("account");
            String accountType = (String) accountDetails.get("type");
            String accountName = (String) accountDetails.get("name");
            String accountMask = (String) accountDetails.get("mask");

            System.out.println("✅ Retrieved bank details from Plaid");
            System.out.println("   Bank: " + (request.getInstitutionName() != null ? request.getInstitutionName() : "Unknown"));
            System.out.println("   Type: " + accountType);
            System.out.println("   Mask: ****" + accountMask);

            // STEP 3: Create Dwolla funding source
            com.example.cube.dto.request.CreateFundingSourceRequest dwollaRequest =
                    new com.example.cube.dto.request.CreateFundingSourceRequest();
            dwollaRequest.setRoutingNumber(routingNumber);
            dwollaRequest.setAccountNumber(accountNumber);
            dwollaRequest.setBankAccountType(mapPlaidTypeToDwolla(accountType));
            dwollaRequest.setName(accountName);

            FundingSourceResponse fundingSource = dwollaFundingSourceService.createFundingSource(
                    userId, dwollaRequest);

            System.out.println("✅ Created Dwolla funding source: " + fundingSource.getFundingSourceId());

            // STEP 4: Build response
            PlaidLinkResponse response = new PlaidLinkResponse(
                    "Bank linked successfully via Plaid",
                    fundingSource.getFundingSourceId()
            );
            response.setFundingSourceStatus(fundingSource.getStatus());
            response.setBankName(request.getInstitutionName());
            response.setAccountType(accountType);
            response.setAccountMask(accountMask);

            return response;

        } catch (Exception e) {
            System.err.println("❌ Failed to link bank via Plaid: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to link bank: " + e.getMessage());
        }
    }

    @Override
    public String createLinkToken(UUID userId) {
        try {
            UserDetails user = userDetailsRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("client_id", plaidClientId);
            requestBody.put("secret", plaidSecret);

            // User info
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("client_user_id", userId.toString());
            requestBody.put("user", userInfo);

            requestBody.put("client_name", "Cube Money");
            requestBody.put("products", Arrays.asList("auth"));
            requestBody.put("country_codes", Arrays.asList("US"));
            requestBody.put("language", "en");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    plaidBaseUrl + "/link/token/create",
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String linkToken = (String) responseBody.get("link_token");

            System.out.println("✅ Created Plaid Link token for user: " + userId);

            return linkToken;

        } catch (Exception e) {
            System.err.println("❌ Failed to create Plaid Link token: " + e.getMessage());
            throw new RuntimeException("Failed to create Plaid Link token: " + e.getMessage());
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Exchange Plaid public token for access token
     */
    private String exchangePublicToken(String publicToken) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("client_id", plaidClientId);
            requestBody.put("secret", plaidSecret);
            requestBody.put("public_token", publicToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    plaidBaseUrl + "/item/public_token/exchange",
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            String accessToken = (String) responseBody.get("access_token");

            System.out.println("✅ Exchanged public token for access token");

            return accessToken;

        } catch (Exception e) {
            System.err.println("❌ Failed to exchange public token: " + e.getMessage());
            throw new RuntimeException("Failed to exchange Plaid token: " + e.getMessage());
        }
    }

    /**
     * Get account details from Plaid
     */
    private Map<String, Object> getAccountDetails(String accessToken, String accountId) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("client_id", plaidClientId);
            requestBody.put("secret", plaidSecret);
            requestBody.put("access_token", accessToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    plaidBaseUrl + "/auth/get",
                    request,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> accounts = (List<Map<String, Object>>) responseBody.get("accounts");
            List<Map<String, Object>> numbers = (List<Map<String, Object>>) responseBody.get("numbers");

            // Find the specific account
            Map<String, Object> account = accounts.stream()
                    .filter(acc -> accountId.equals(acc.get("account_id")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Find ACH numbers for this account
            Map<String, Object> achNumbers = numbers.stream()
                    .filter(num -> accountId.equals(num.get("account_id")))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("ACH numbers not found"));

            // Extract details
            Map<String, Object> result = new HashMap<>();
            result.put("routing", achNumbers.get("routing"));
            result.put("account", achNumbers.get("account"));
            result.put("type", account.get("subtype")); // checking, savings, etc.
            result.put("name", account.get("name"));
            result.put("mask", account.get("mask"));

            return result;

        } catch (Exception e) {
            System.err.println("❌ Failed to get account details: " + e.getMessage());
            throw new RuntimeException("Failed to get bank account details: " + e.getMessage());
        }
    }

    /**
     * Map Plaid account types to Dwolla types
     */
    private String mapPlaidTypeToDwolla(String plaidType) {
        if (plaidType == null) return "checking";

        switch (plaidType.toLowerCase()) {
            case "checking":
                return "checking";
            case "savings":
                return "savings";
            default:
                return "checking"; // Default to checking
        }
    }
}