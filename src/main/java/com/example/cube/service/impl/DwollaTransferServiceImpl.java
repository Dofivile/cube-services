package com.example.cube.service.impl;

import com.example.cube.dto.response.TransferResponse;
import com.example.cube.model.*;
import com.example.cube.repository.*;
import com.example.cube.service.DwollaTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DwollaTransferServiceImpl implements DwollaTransferService {

    @Value("${DWOLLA_KEY}")
    private String dwollaKey;

    @Value("${DWOLLA_SECRET}")
    private String dwollaSecret;

    @Value("${DWOLLA_BASE_URL}")
    private String dwollaBaseUrl;

    // TODO: You'll need to create this - your platform's Dwolla master account funding source
    @Value("${DWOLLA_PLATFORM_FUNDING_SOURCE_ID:}")
    private String platformFundingSourceId;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CubeRepository cubeRepository;

    @Autowired
    private CubeMemberRepository cubeMemberRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get OAuth token
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

        ResponseEntity<Map> response = restTemplate.postForEntity(
                dwollaBaseUrl + "/token", request, Map.class);

        return (String) response.getBody().get("access_token");
    }

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
    public TransferResponse createCubeContribution(UUID userId, UUID cubeId, UUID memberId, Integer cycleNumber) {

        System.out.println("\n💰 ========== CREATING CUBE CONTRIBUTION ==========");
        System.out.println("User: " + userId);
        System.out.println("Cube: " + cubeId);
        System.out.println("Cycle: " + cycleNumber);

        // 1. Validate cube
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Validate member
        CubeMember member = cubeMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getCubeId().equals(cubeId) || !member.getUserId().equals(userId)) {
            throw new RuntimeException("Invalid member for this cube/user");
        }

        // 3. Validate cycle
        if (!cycleNumber.equals(cube.getCurrentCycle())) {
            throw new RuntimeException("Invalid cycle. Current cycle: " + cube.getCurrentCycle());
        }

        // 4. Check if already paid
        boolean alreadyPaid = paymentTransactionRepository
                .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId, memberId, cycleNumber, 1, 2);

        if (alreadyPaid) {
            throw new RuntimeException("Payment already recorded for this cycle");
        }

        // 5. Get user's funding source
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaFundingSourceId() == null) {
            throw new RuntimeException("User must have a verified bank account");
        }

        // 6. Get amount
        BigDecimal amount = cube.getAmountPerCycle();

        // 7. Create transfer
        try {
            Map<String, Object> transferData = new HashMap<>();

            // Links to source and destination
            Map<String, Map<String, String>> links = new HashMap<>();

            Map<String, String> source = new HashMap<>();
            source.put("href", dwollaBaseUrl + "/funding-sources/" + user.getDwollaFundingSourceId());
            links.put("source", source);

            Map<String, String> destination = new HashMap<>();
            destination.put("href", dwollaBaseUrl + "/funding-sources/" + platformFundingSourceId);
            links.put("destination", destination);

            transferData.put("_links", links);

            // Amount
            Map<String, String> amountMap = new HashMap<>();
            amountMap.put("currency", "USD");
            amountMap.put("value", amount.toString());
            transferData.put("amount", amountMap);

            // Metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_id", userId.toString());
            metadata.put("cube_id", cubeId.toString());
            metadata.put("member_id", memberId.toString());
            metadata.put("cycle_number", cycleNumber.toString());
            metadata.put("type", "contribution");
            transferData.put("metadata", metadata);

            // Correlation ID (for idempotency)
            String correlationId = "contribution-" + cubeId + "-" + memberId + "-" + cycleNumber;
            transferData.put("correlationId", correlationId);

            HttpHeaders headers = createAuthHeaders();
            headers.set("Idempotency-Key", correlationId); // Prevent duplicate transfers
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(transferData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    dwollaBaseUrl + "/transfers",
                    requestEntity,
                    String.class
            );

            String transferUrl = response.getHeaders().getLocation().toString();
            String transferId = extractIdFromUrl(transferUrl);

            System.out.println("✅ Created transfer: " + transferId);

        // Replace this section (around line 8 in the transaction creation):
        // 8. Create payment transaction record (pending)
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setUserId(userId);
            transaction.setCubeId(cubeId);
            transaction.setMemberId(memberId);
            transaction.setDwollaTransferId(transferId);  // ✅ Use Dwolla field
            transaction.setDwollaTransferUrl(transferUrl); // ✅ Store URL
            transaction.setDwollaCorrelationId(correlationId); // ✅ Store correlation ID
            transaction.setAmount(amount);
            transaction.setCycleNumber(cycleNumber);
            transaction.setTypeId(1); // contribution
            transaction.setStatusId(1); // pending
            transaction.setCreatedAt(LocalDateTime.now());

            paymentTransactionRepository.save(transaction);

            // 9. Get transfer details
            Map<String, Object> transferDetails = getTransferDetails(transferId);
            TransferResponse transferResponse = mapToTransferResponse(transferDetails);
            transferResponse.setMessage("Transfer initiated. Funds will arrive in 1-3 business days.");

            return transferResponse;

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Dwolla API error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create transfer: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Error creating transfer: " + e.getMessage());
            throw new RuntimeException("Failed to create transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public TransferResponse sendPayoutToWinner(UUID winnerId, BigDecimal amount, UUID cubeId, Integer cycleNumber) {

        System.out.println("\n💸 ========== SENDING PAYOUT TO WINNER ==========");
        System.out.println("Winner: " + winnerId);
        System.out.println("Amount: $" + amount);
        System.out.println("Cube: " + cubeId);
        System.out.println("Cycle: " + cycleNumber);

        // 1. Get winner details
        UserDetails winner = userDetailsRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));

        if (winner.getDwollaFundingSourceId() == null) {
            throw new RuntimeException("Winner must have a verified bank account");
        }

        // 2. Get member record
        CubeMember member = cubeMemberRepository.findByCubeIdAndUserId(cubeId, winnerId)
                .orElseThrow(() -> new RuntimeException("Member not found in cube"));

        // 3. Check if payout already sent
        boolean alreadyPaid = paymentTransactionRepository
                .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId, member.getMemberId(), cycleNumber, 2, 2);

        if (alreadyPaid) {
            throw new RuntimeException("Payout already sent for this cycle");
        }

        // 4. Create transfer
        try {
            Map<String, Object> transferData = new HashMap<>();

            // Links
            Map<String, Map<String, String>> links = new HashMap<>();

            Map<String, String> source = new HashMap<>();
            source.put("href", dwollaBaseUrl + "/funding-sources/" + platformFundingSourceId);
            links.put("source", source);

            Map<String, String> destination = new HashMap<>();
            destination.put("href", dwollaBaseUrl + "/funding-sources/" + winner.getDwollaFundingSourceId());
            links.put("destination", destination);

            transferData.put("_links", links);

            // Amount
            Map<String, String> amountMap = new HashMap<>();
            amountMap.put("currency", "USD");
            amountMap.put("value", amount.toString());
            transferData.put("amount", amountMap);

            // Metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("winner_id", winnerId.toString());
            metadata.put("cube_id", cubeId.toString());
            metadata.put("cycle_number", cycleNumber.toString());
            metadata.put("type", "payout");
            transferData.put("metadata", metadata);

            // Correlation ID
            String correlationId = "payout-" + cubeId + "-" + winnerId + "-" + cycleNumber;
            transferData.put("correlationId", correlationId);

            HttpHeaders headers = createAuthHeaders();
            headers.set("Idempotency-Key", correlationId);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(transferData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    dwollaBaseUrl + "/transfers",
                    requestEntity,
                    String.class
            );

            String transferUrl = response.getHeaders().getLocation().toString();
            String transferId = extractIdFromUrl(transferUrl);

            System.out.println("✅ Created payout transfer: " + transferId);

            // Replace this section (around line 5 in the transaction creation):
            // 5. Create payout transaction record
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setUserId(winnerId);
            transaction.setCubeId(cubeId);
            transaction.setMemberId(member.getMemberId());
            transaction.setDwollaTransferId(transferId);  // ✅ Use Dwolla field
            transaction.setDwollaTransferUrl(transferUrl); // ✅ Store URL
            transaction.setDwollaCorrelationId(correlationId); // ✅ Store correlation ID
            transaction.setAmount(amount);
            transaction.setCycleNumber(cycleNumber);
            transaction.setTypeId(2); // payout
            transaction.setStatusId(1); // pending
            transaction.setCreatedAt(LocalDateTime.now());

            paymentTransactionRepository.save(transaction);
            // 6. Get transfer details
            Map<String, Object> transferDetails = getTransferDetails(transferId);
            TransferResponse transferResponse = mapToTransferResponse(transferDetails);
            transferResponse.setMessage("Payout initiated. Funds will arrive in 1-3 business days.");

            return transferResponse;

        } catch (HttpClientErrorException e) {
            System.err.println("❌ Dwolla API error: " + e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send payout: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("❌ Error sending payout: " + e.getMessage());
            throw new RuntimeException("Failed to send payout: " + e.getMessage());
        }
    }

    @Override
    public TransferResponse getTransferStatus(String transferId) {
        try {
            Map<String, Object> transferDetails = getTransferDetails(transferId);
            return mapToTransferResponse(transferDetails);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get transfer status: " + e.getMessage());
        }
    }

    @Override
    public List<TransferResponse> listCustomerTransfers(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDwollaCustomerId() == null) {
            throw new RuntimeException("User does not have a Dwolla account");
        }

        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = dwollaBaseUrl + "/customers/" + user.getDwollaCustomerId() + "/transfers";

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> embedded = (Map<String, Object>) responseBody.get("_embedded");

            if (embedded == null || !embedded.containsKey("transfers")) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> transfers = (List<Map<String, Object>>) embedded.get("transfers");

            return transfers.stream()
                    .map(this::mapToTransferResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to list transfers: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancelTransfer(String transferId) {
        try {
            Map<String, Object> cancelData = new HashMap<>();
            cancelData.put("status", "cancelled");

            HttpHeaders headers = createAuthHeaders();
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(cancelData, headers);

            String url = dwollaBaseUrl + "/transfers/" + transferId;

            restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            System.out.println("✅ Cancelled transfer: " + transferId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel transfer: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleTransferWebhook(String transferId, String status) {
        System.out.println("📨 Handling transfer webhook: " + transferId + " - Status: " + status);

        // Find transaction by Dwolla transfer ID ✅
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository
                .findByDwollaTransferId(transferId);  // ✅ Changed from findByStripePaymentIntentId

        if (transactionOpt.isEmpty()) {
            System.err.println("⚠️ Transaction not found for transfer: " + transferId);
            return;
        }

        PaymentTransaction transaction = transactionOpt.get();

        switch (status) {
            case "processed":
                transaction.setStatusId(2); // completed
                transaction.setProcessedAt(LocalDateTime.now());

                // Update cube total if contribution
                if (transaction.getTypeId() == 1) {
                    Cube cube = cubeRepository.findById(transaction.getCubeId()).orElse(null);
                    if (cube != null) {
                        cube.setTotalAmountCollected(
                                cube.getTotalAmountCollected().add(transaction.getAmount())
                        );
                        cubeRepository.save(cube);
                    }
                }

                System.out.println("✅ Transfer processed successfully");
                break;

            case "failed":
                transaction.setStatusId(3); // failed
                System.err.println("❌ Transfer failed");
                break;

            case "cancelled":
                transaction.setStatusId(4); // cancelled
                System.out.println("⚠️ Transfer cancelled");
                break;

            default:
                System.out.println("ℹ️ Transfer status: " + status);
        }

        paymentTransactionRepository.save(transaction);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Map<String, Object> getTransferDetails(String transferId) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = dwollaBaseUrl + "/transfers/" + transferId;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        return response.getBody();
    }

    private TransferResponse mapToTransferResponse(Map<String, Object> transfer) {
        TransferResponse response = new TransferResponse();
        response.setTransferId((String) transfer.get("id"));
        response.setStatus((String) transfer.get("status"));
        response.setCorrelationId((String) transfer.get("correlationId"));

        // Amount
        if (transfer.containsKey("amount")) {
            Map<String, String> amountMap = (Map<String, String>) transfer.get("amount");
            response.setAmount(new BigDecimal(amountMap.get("value")));
        }

        // Created date
        if (transfer.containsKey("created")) {
            String createdStr = (String) transfer.get("created");
            response.setCreated(LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_DATE_TIME));
        }

        // Metadata
        if (transfer.containsKey("metadata")) {
            response.setMetadata((Map<String, String>) transfer.get("metadata"));
        }

        // Failure details
        if (transfer.containsKey("failure")) {
            Map<String, String> failure = (Map<String, String>) transfer.get("failure");
            response.setFailureCode(failure.get("code"));
            response.setFailureDescription(failure.get("description"));
        }

        // Extract URLs from _links
        if (transfer.containsKey("_links")) {
            Map<String, Object> links = (Map<String, Object>) transfer.get("_links");

            if (links.containsKey("self")) {
                Map<String, String> self = (Map<String, String>) links.get("self");
                response.setTransferUrl(self.get("href"));
            }

            if (links.containsKey("source")) {
                Map<String, String> source = (Map<String, String>) links.get("source");
                response.setSourceFundingSourceId(extractIdFromUrl(source.get("href")));
            }

            if (links.containsKey("destination")) {
                Map<String, String> destination = (Map<String, String>) links.get("destination");
                response.setDestinationFundingSourceId(extractIdFromUrl(destination.get("href")));
            }
        }

        return response;
    }

    private String extractIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
}