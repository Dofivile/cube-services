package com.example.cube.service;

import com.example.cube.dto.request.CreateDwollaCustomerRequest;
import com.example.cube.dto.response.DwollaCustomerResponse;
import com.example.cube.dto.response.DwollaDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

public interface DwollaCustomerService {

    /**
     * Create a personal verified customer in Dwolla
     */
    DwollaCustomerResponse createPersonalVerifiedCustomer(UUID userId, CreateDwollaCustomerRequest request);

    /**
     * Retrieve customer status from Dwolla
     */
    DwollaCustomerResponse getCustomerStatus(UUID userId);

    /**
     * Retry verification with updated information (for retry status)
     */
    DwollaCustomerResponse retryCustomerVerification(UUID userId, CreateDwollaCustomerRequest request);

    /**
     * Upload verification document (for document status)
     */
    DwollaDocumentResponse uploadVerificationDocument(UUID userId, String documentType, MultipartFile file);

    /**
     * Retrieve document status
     */
    DwollaDocumentResponse getDocumentStatus(String documentId);

    /**
     * Initiate KBA session (for kba status)
     */
    String initiateKbaSession(UUID userId);

    /**
     * Retrieve KBA questions
     */
    Map<String, Object> getKbaQuestions(String kbaId);

    /**
     * Submit KBA answers
     */
    void submitKbaAnswers(String kbaId, Map<String, Object> answers);
}