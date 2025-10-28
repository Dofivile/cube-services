package com.example.cube.service.impl;

import com.example.cube.dto.request.CreateDwollaCustomerRequest;
import com.example.cube.dto.response.DwollaCustomerResponse;
import com.example.cube.dto.response.DwollaDocumentResponse;
import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.DwollaCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DwollaCustomerServiceImpl implements DwollaCustomerService {

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
    public DwollaCustomerResponse createPersonalVerifiedCustomer(UUID userId, CreateDwollaCustomerRequest request) {

        // Check if user already has a Dwolla customer
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() != null) {
            throw new RuntimeException("User already has a Dwolla customer account");
        }

        // Build request body
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("firstName", request.getFirstName());
        customerData.put("lastName", request.getLastName());
        customerData.put("email", request.getEmail());
        customerData.put("type", "personal");
        customerData.put("address1", request.getAddress1());
        customerData.put("city", request.getCity());
        customerData.put("state", request.getState());
        customerData.put("postalCode", request.getPostalCode());
        customerData.put("dateOfBirth", request.getDateOfBirth());
        customerData.put("ssn", request.getSsn());

        // Optional fields
        if (request.getAddress2() != null && !request.getAddress2().isEmpty()) {
            customerData.put("address2", request.getAddress2());
        }
        if (request.getIpAddress() != null && !request.getIpAddress().isEmpty()) {
            customerData.put("ipAddress", request.getIpAddress());
        }
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            customerData.put("phone", request.getPhone());
        }
        if (request.getCorrelationId() != null && !request.getCorrelationId().isEmpty()) {
            customerData.put("correlationId", request.getCorrelationId());
        }

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(customerData, headers);

        try {
            // Create customer in Dwolla
            ResponseEntity<String> response = restTemplate.postForEntity(
                    dwollaBaseUrl + "/customers",
                    requestEntity,
                    String.class
            );

            // Extract customer URL from Location header
            String customerUrl = response.getHeaders().getLocation().toString();
            String customerId = extractCustomerIdFromUrl(customerUrl);

            System.out.println("✅ Created Dwolla customer: " + customerId + " for user: " + userId);

            // Retrieve customer details to get status
            Map<String, Object> customerDetails = getCustomerDetails(customerId);
            String status = (String) customerDetails.get("status");

            // Update user record
            user.setDwollaCustomerId(customerId);
            user.setDwollaStatus(status);
            userDetailsRepository.save(user);

            // Build response
            DwollaCustomerResponse dwollaResponse = new DwollaCustomerResponse(customerId, status, customerUrl);
            dwollaResponse.setFirstName(request.getFirstName());
            dwollaResponse.setLastName(request.getLastName());
            dwollaResponse.setEmail(request.getEmail());
            dwollaResponse.setMessage(getStatusMessage(status));

            return dwollaResponse;

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Dwolla API error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create Dwolla customer: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Error creating Dwolla customer: " + e.getMessage());
            throw new RuntimeException("Failed to create Dwolla customer: " + e.getMessage());
        }
    }

    @Override
    public DwollaCustomerResponse getCustomerStatus(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        try {
            Map<String, Object> customerDetails = getCustomerDetails(user.getDwollaCustomerId());
            String status = (String) customerDetails.get("status");

            // Update status in database
            user.setDwollaStatus(status);
            userDetailsRepository.save(user);

            DwollaCustomerResponse response = new DwollaCustomerResponse();
            response.setCustomerId(user.getDwollaCustomerId());
            response.setStatus(status);
            response.setFirstName((String) customerDetails.get("firstName"));
            response.setLastName((String) customerDetails.get("lastName"));
            response.setEmail((String) customerDetails.get("email"));
            response.setMessage(getStatusMessage(status));

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve customer status: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DwollaCustomerResponse retryCustomerVerification(UUID userId, CreateDwollaCustomerRequest request) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        if (!"retry".equals(user.getDwollaStatus())) {
            throw new RuntimeException("Customer is not in retry status. Current status: " + user.getDwollaStatus());
        }

        // Build update request - must include full SSN for retry
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("firstName", request.getFirstName());
        customerData.put("lastName", request.getLastName());
        customerData.put("email", request.getEmail());
        customerData.put("ipAddress", request.getIpAddress());
        customerData.put("type", "personal");
        customerData.put("address1", request.getAddress1());
        customerData.put("city", request.getCity());
        customerData.put("state", request.getState());
        customerData.put("postalCode", request.getPostalCode());
        customerData.put("dateOfBirth", request.getDateOfBirth());
        customerData.put("ssn", request.getSsn()); // Must be full 9 digits

        if (request.getAddress2() != null) {
            customerData.put("address2", request.getAddress2());
        }
        if (request.getPhone() != null) {
            customerData.put("phone", request.getPhone());
        }

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(customerData, headers);

        try {
            String url = dwollaBaseUrl + "/customers/" + user.getDwollaCustomerId();

            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("✅ Retried verification for customer: " + user.getDwollaCustomerId());

            // Get updated status
            Map<String, Object> customerDetails = getCustomerDetails(user.getDwollaCustomerId());
            String newStatus = (String) customerDetails.get("status");

            user.setDwollaStatus(newStatus);
            userDetailsRepository.save(user);

            DwollaCustomerResponse response = new DwollaCustomerResponse();
            response.setCustomerId(user.getDwollaCustomerId());
            response.setStatus(newStatus);
            response.setMessage(getStatusMessage(newStatus));

            return response;

        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to retry verification: " + e.getResponseBodyAsString());
        }
    }

    @Override
    @Transactional
    public DwollaDocumentResponse uploadVerificationDocument(UUID userId, String documentType, MultipartFile file) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        if (!"document".equals(user.getDwollaStatus())) {
            throw new RuntimeException("Customer is not in document status. Current status: " + user.getDwollaStatus());
        }

        // Validate document type
        if (!Arrays.asList("passport", "license", "idCard").contains(documentType)) {
            throw new RuntimeException("Invalid document type. Must be: passport, license, or idCard");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/jpg", "image/png").contains(contentType)) {
            throw new RuntimeException("Invalid file type. Must be JPG or PNG");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        try {
            String token = getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/vnd.dwolla.v1.hal+json");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("documentType", documentType);
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = dwollaBaseUrl + "/customers/" + user.getDwollaCustomerId() + "/documents";

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            String documentUrl = response.getHeaders().getLocation().toString();
            String documentId = extractDocumentIdFromUrl(documentUrl);

            System.out.println("✅ Uploaded document: " + documentId + " for customer: " + user.getDwollaCustomerId());

            DwollaDocumentResponse documentResponse = new DwollaDocumentResponse();
            documentResponse.setDocumentId(documentId);
            documentResponse.setType(documentType);
            documentResponse.setStatus("pending");

            return documentResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    public DwollaDocumentResponse getDocumentStatus(String documentId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = dwollaBaseUrl + "/documents/" + documentId;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> document = response.getBody();

            DwollaDocumentResponse documentResponse = new DwollaDocumentResponse();
            documentResponse.setDocumentId((String) document.get("id"));
            documentResponse.setStatus((String) document.get("status"));
            documentResponse.setType((String) document.get("type"));
            documentResponse.setFailureReason((String) document.get("failureReason"));

            // Handle multiple failure reasons if present
            if (document.containsKey("allFailureReasons")) {
                List<Map<String, String>> failures = (List<Map<String, String>>) document.get("allFailureReasons");
                List<DwollaDocumentResponse.FailureDetail> failureDetails = new ArrayList<>();

                for (Map<String, String> failure : failures) {
                    failureDetails.add(new DwollaDocumentResponse.FailureDetail(
                            failure.get("reason"),
                            failure.get("description")
                    ));
                }

                documentResponse.setAllFailureReasons(failureDetails);
            }

            return documentResponse;

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve document status: " + e.getMessage());
        }
    }

    @Override
    public String initiateKbaSession(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla customer account");
        }

        if (!"kba".equals(user.getDwollaStatus())) {
            throw new RuntimeException("Customer is not in KBA status. Current status: " + user.getDwollaStatus());
        }

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = dwollaBaseUrl + "/customers/" + user.getDwollaCustomerId() + "/kba";

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            String kbaUrl = response.getHeaders().getLocation().toString();
            String kbaId = extractKbaIdFromUrl(kbaUrl);

            System.out.println("✅ Initiated KBA session: " + kbaId + " for customer: " + user.getDwollaCustomerId());

            return kbaId;

        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate KBA session: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getKbaQuestions(String kbaId) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = dwollaBaseUrl + "/kba/" + kbaId;

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve KBA questions: " + e.getMessage());
        }
    }

    @Override
    public void submitKbaAnswers(String kbaId, Map<String, Object> answers) {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(answers, headers);

            String url = dwollaBaseUrl + "/kba/" + kbaId;

            restTemplate.postForEntity(url, requestEntity, String.class);

            System.out.println("✅ Submitted KBA answers for session: " + kbaId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to submit KBA answers: " + e.getMessage());
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Map<String, Object> getCustomerDetails(String customerId) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = dwollaBaseUrl + "/customers/" + customerId;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        return response.getBody();
    }

    private String extractCustomerIdFromUrl(String url) {
        // Extract UUID from URL like: https://api-sandbox.dwolla.com/customers/FC451A7A-AE30-4404-AB95-E3553FCD733F
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String extractDocumentIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String extractKbaIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String getStatusMessage(String status) {
        switch (status) {
            case "verified":
                return "Customer is successfully verified and can send/receive funds";
            case "retry":
                return "Initial verification failed. Please retry with corrected information and full SSN";
            case "document":
                return "Additional documentation required. Please upload a photo ID";
            case "kba":
                return "Knowledge-based authentication required. Customer must answer identity verification questions";
            case "suspended":
                return "Customer account is suspended. Please contact support";
            default:
                return "Customer status: " + status;
        }
    }
}