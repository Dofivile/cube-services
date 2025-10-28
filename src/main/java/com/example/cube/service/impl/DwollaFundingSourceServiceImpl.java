package com.example.cube.service.impl;

import com.example.cube.dto.request.CreateFundingSourceRequest;
import com.example.cube.dto.request.VerifyMicroDepositsRequest;
import com.example.cube.dto.response.FundingSourceResponse;
import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.DwollaFundingSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DwollaFundingSourceServiceImpl implements DwollaFundingSourceService {

    @Value("${DWOLLA_KEY}")
    private String dwollaKey;

    @Value("${DWOLLA_SECRET}")
    private String dwollaSecret;

    @Value("${DWOLLA_BASE_URL}")
    private String dwollaBaseUrl;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get OAuth token for Dwolla API
     */
    private String getAccessToken() {
        String auth = dwollaKey + ":" + dwollaSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    dwollaBaseUrl + "/token",
                    request,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                return (String) response.getBody().get("access_token");
            }

            throw new RuntimeException("Failed to obtain Dwolla access token");

        } catch (Exception e) {
            throw new RuntimeException("Error getting Dwolla access token: " + e.getMessage());
        }
    }

    /**
     * Create authorization headers with Bearer token
     */
    private HttpHeaders createAuthHeaders() {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/vnd.dwolla.v1.hal+json");
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    @Override
    @Transactional
    public FundingSourceResponse createFundingSource(UUID userId, CreateFundingSourceRequest request) {

        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User must have a Dwolla customer account before adding a bank");
        }

        // Build request body
        Map<String, Object> fundingSourceData = new HashMap<>();
        fundingSourceData.put("routingNumber", request.getRoutingNumber());
        fundingSourceData.put("accountNumber", request.getAccountNumber());
        fundingSourceData.put("bankAccountType", request.getBankAccountType());
        fundingSourceData.put("name", request.getName());

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(fundingSourceData, headers);

        try {
            String url = dwollaBaseUrl + "/customers/" + user.getDwollaCustomerId() + "/funding-sources";

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            // Extract funding source URL from Location header
            String fundingSourceUrl = response.getHeaders().getLocation().toString();
            String fundingSourceId = extractIdFromUrl(fundingSourceUrl);

            System.out.println("✅ Created funding source: " + fundingSourceId + " for user: " + userId);

            // Get funding source details
            Map<String, Object> fundingSourceDetails = getFundingSourceDetails(fundingSourceId);

            // Store primary funding source ID in user record
            if (user.getDwollaFundingSourceId() == null) {
                user.setDwollaFundingSourceId(fundingSourceId);
                userDetailsRepository.save(user);
            }

            return mapToFundingSourceResponse(fundingSourceDetails);

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Dwolla API error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create funding source: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Error creating funding source: " + e.getMessage());
            throw new RuntimeException("Failed to create funding source: " + e.getMessage());
        }
    }

    @Override
    public List<FundingSourceResponse> listFundingSources(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = dwollaBaseUrl + "/customers/" + user.getDwollaCustomerId() + "/funding-sources";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> embedded = (Map<String, Object>) responseBody.get("_embedded");

            if (embedded == null || !embedded.containsKey("funding-sources")) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> fundingSources = (List<Map<String, Object>>) embedded.get("funding-sources");

            return fundingSources.stream()
                    .filter(fs -> "bank".equals(fs.get("type"))) // Filter only bank accounts
                    .map(this::mapToFundingSourceResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to list funding sources: " + e.getMessage());
        }
    }

    @Override
    public FundingSourceResponse getFundingSource(String fundingSourceId) {
        try {
            Map<String, Object> fundingSourceDetails = getFundingSourceDetails(fundingSourceId);
            return mapToFundingSourceResponse(fundingSourceDetails);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get funding source: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FundingSourceResponse initiateMicroDeposits(UUID userId, String fundingSourceId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = dwollaBaseUrl + "/funding-sources/" + fundingSourceId + "/micro-deposits";

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            System.out.println("✅ Initiated micro-deposits for funding source: " + fundingSourceId);

            // Get updated funding source details
            Map<String, Object> fundingSourceDetails = getFundingSourceDetails(fundingSourceId);
            FundingSourceResponse fundingSourceResponse = mapToFundingSourceResponse(fundingSourceDetails);
            fundingSourceResponse.setMessage("Micro-deposits initiated. Please check your bank account in 1-2 business days.");

            return fundingSourceResponse;

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to initiate micro-deposits: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate micro-deposits: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public FundingSourceResponse verifyMicroDeposits(UUID userId, String fundingSourceId, VerifyMicroDepositsRequest request) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        try {
            // Build verification request
            Map<String, Object> verificationData = new HashMap<>();
            Map<String, String> amount1 = new HashMap<>();
            amount1.put("value", request.getAmount1().toString());
            amount1.put("currency", "USD");

            Map<String, String> amount2 = new HashMap<>();
            amount2.put("value", request.getAmount2().toString());
            amount2.put("currency", "USD");

            verificationData.put("amount1", amount1);
            verificationData.put("amount2", amount2);

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(verificationData, headers);

            String url = dwollaBaseUrl + "/funding-sources/" + fundingSourceId + "/micro-deposits";

            restTemplate.postForEntity(url, requestEntity, String.class);

            System.out.println("✅ Verified micro-deposits for funding source: " + fundingSourceId);

            // Get updated funding source details
            Map<String, Object> fundingSourceDetails = getFundingSourceDetails(fundingSourceId);
            FundingSourceResponse fundingSourceResponse = mapToFundingSourceResponse(fundingSourceDetails);
            fundingSourceResponse.setMessage("Bank account verified successfully!");

            return fundingSourceResponse;

        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            if (errorMessage.contains("InvalidAmount")) {
                throw new RuntimeException("Incorrect micro-deposit amounts. Please try again.");
            }
            throw new RuntimeException("Failed to verify micro-deposits: " + errorMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify micro-deposits: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeFundingSource(UUID userId, String fundingSourceId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("removed", true);

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(updateData, headers);

            String url = dwollaBaseUrl + "/funding-sources/" + fundingSourceId;

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("✅ Removed funding source: " + fundingSourceId);

            // If this was the primary funding source, clear it
            if (fundingSourceId.equals(user.getDwollaFundingSourceId())) {
                user.setDwollaFundingSourceId(null);
                userDetailsRepository.save(user);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove funding source: " + e.getMessage());
        }
    }

    @Override
    public FundingSourceResponse getPrimaryVerifiedFundingSource(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaFundingSourceId() != null) {
            return getFundingSource(user.getDwollaFundingSourceId());
        }

        // Find first verified funding source
        List<FundingSourceResponse> fundingSources = listFundingSources(userId);

        Optional<FundingSourceResponse> verifiedSource = fundingSources.stream()
                .filter(fs -> "verified".equals(fs.getStatus()))
                .findFirst();

        if (verifiedSource.isPresent()) {
            FundingSourceResponse source = verifiedSource.get();
            // Update user record with this as primary
            user.setDwollaFundingSourceId(source.getFundingSourceId());
            userDetailsRepository.save(user);
            return source;
        }

        throw new RuntimeException("No verified funding source found for user");
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Map<String, Object> getFundingSourceDetails(String fundingSourceId) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = dwollaBaseUrl + "/funding-sources/" + fundingSourceId;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        return response.getBody();
    }

    private FundingSourceResponse mapToFundingSourceResponse(Map<String, Object> fundingSource) {
        FundingSourceResponse response = new FundingSourceResponse();
        response.setFundingSourceId((String) fundingSource.get("id"));
        response.setStatus((String) fundingSource.get("status"));
        response.setType((String) fundingSource.get("type"));
        response.setBankAccountType((String) fundingSource.get("bankAccountType"));
        response.setName((String) fundingSource.get("name"));
        response.setBankName((String) fundingSource.get("bankName"));

        // Parse created date
        if (fundingSource.containsKey("created")) {
            String createdStr = (String) fundingSource.get("created");
            response.setCreated(LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_DATE_TIME));
        }

        // Extract funding source URL from _links
        if (fundingSource.containsKey("_links")) {
            Map<String, Object> links = (Map<String, Object>) fundingSource.get("_links");
            if (links.containsKey("self")) {
                Map<String, String> self = (Map<String, String>) links.get("self");
                response.setFundingSourceUrl(self.get("href"));
            }
        }

        // Add status message
        response.setMessage(getFundingSourceStatusMessage((String) fundingSource.get("status")));

        return response;
    }

    private String extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String getFundingSourceStatusMessage(String status) {
        switch (status) {
            case "verified":
                return "Bank account is verified and ready to send/receive funds";
            case "unverified":
                return "Bank account is unverified. Can receive funds only. Initiate micro-deposits to verify.";
            case "removed":
                return "Bank account has been removed";
            default:
                return "Bank account status: " + status;
        }
    }
}