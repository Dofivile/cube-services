package com.example.cube.controller;

import com.example.cube.dto.request.CreateDwollaCustomerRequest;
import com.example.cube.dto.response.DwollaCustomerResponse;
import com.example.cube.dto.response.DwollaDocumentResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.DwollaCustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dwolla")
public class DwollaController {

    private final DwollaCustomerService dwollaCustomerService;
    private final AuthenticationService authenticationService;

    @Autowired
    public DwollaController(
            DwollaCustomerService dwollaCustomerService,
            AuthenticationService authenticationService) {
        this.dwollaCustomerService = dwollaCustomerService;
        this.authenticationService = authenticationService;
    }

    // ==================== CUSTOMER CREATION ====================

    @PostMapping("/customers/create")
    public ResponseEntity<DwollaCustomerResponse> createPersonalVerifiedCustomer(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateDwollaCustomerRequest request) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("📝 Creating Dwolla personal verified customer for user: " + userId);

            DwollaCustomerResponse response = dwollaCustomerService.createPersonalVerifiedCustomer(userId, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to create Dwolla customer: " + e.getMessage());
            throw new RuntimeException("Failed to create Dwolla customer: " + e.getMessage());
        }
    }

    // ==================== CUSTOMER STATUS ====================

    @GetMapping("/customers/status")
    public ResponseEntity<DwollaCustomerResponse> getCustomerStatus(
            @RequestHeader("Authorization") String authHeader) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            DwollaCustomerResponse response = dwollaCustomerService.getCustomerStatus(userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to get customer status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== RETRY VERIFICATION ====================

    @PostMapping("/customers/retry")
    public ResponseEntity<DwollaCustomerResponse> retryVerification(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CreateDwollaCustomerRequest request) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("🔄 Retrying verification for user: " + userId);

            DwollaCustomerResponse response = dwollaCustomerService.retryCustomerVerification(userId, request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to retry verification: " + e.getMessage());
            throw new RuntimeException("Failed to retry verification: " + e.getMessage());
        }
    }

    // ==================== DOCUMENT UPLOAD ====================

    @PostMapping("/customers/documents/upload")
    public ResponseEntity<DwollaDocumentResponse> uploadDocument(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("📄 Uploading document for user: " + userId);

            DwollaDocumentResponse response = dwollaCustomerService.uploadVerificationDocument(
                    userId, documentType, file);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to upload document: " + e.getMessage());
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }

    @GetMapping("/documents/{documentId}/status")
    public ResponseEntity<DwollaDocumentResponse> getDocumentStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String documentId) {

        try {
            authenticationService.validateAndExtractUserId(authHeader);
            DwollaDocumentResponse response = dwollaCustomerService.getDocumentStatus(documentId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to get document status: " + e.getMessage());
            throw new RuntimeException("Failed to get document status: " + e.getMessage());
        }
    }

    // ==================== KBA (Knowledge-Based Authentication) ====================

    @PostMapping("/customers/kba/initiate")
    public ResponseEntity<Map<String, String>> initiateKba(
            @RequestHeader("Authorization") String authHeader) {

        try {
            UUID userId = authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("🔐 Initiating KBA for user: " + userId);

            String kbaId = dwollaCustomerService.initiateKbaSession(userId);

            Map<String, String> response = new HashMap<>();
            response.put("kbaId", kbaId);
            response.put("message", "KBA session initiated. Retrieve questions using this ID");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to initiate KBA: " + e.getMessage());
            throw new RuntimeException("Failed to initiate KBA: " + e.getMessage());
        }
    }

    @GetMapping("/kba/{kbaId}/questions")
    public ResponseEntity<Map<String, Object>> getKbaQuestions(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String kbaId) {

        try {
            authenticationService.validateAndExtractUserId(authHeader);
            Map<String, Object> questions = dwollaCustomerService.getKbaQuestions(kbaId);

            return ResponseEntity.ok(questions);

        } catch (Exception e) {
            System.err.println("❌ Failed to get KBA questions: " + e.getMessage());
            throw new RuntimeException("Failed to get KBA questions: " + e.getMessage());
        }
    }

    @PostMapping("/kba/{kbaId}/answers")
    public ResponseEntity<Map<String, String>> submitKbaAnswers(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String kbaId,
            @RequestBody Map<String, Object> answers) {

        try {
            authenticationService.validateAndExtractUserId(authHeader);
            System.out.println("📝 Submitting KBA answers for session: " + kbaId);

            dwollaCustomerService.submitKbaAnswers(kbaId, answers);

            Map<String, String> response = new HashMap<>();
            response.put("message", "KBA answers submitted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Failed to submit KBA answers: " + e.getMessage());
            throw new RuntimeException("Failed to submit KBA answers: " + e.getMessage());
        }
    }

    // ==================== HELPER METHODS ====================

    private DwollaCustomerResponse createErrorResponse(String message) {
        DwollaCustomerResponse response = new DwollaCustomerResponse();
        response.setMessage(message);
        return response;
    }
}