package com.example.cube.dto.response;

import java.util.List;

public class DwollaDocumentResponse {
    private String documentId;
    private String status;
    private String type;
    private String failureReason;
    private List<FailureDetail> allFailureReasons;

    public static class FailureDetail {
        private String reason;
        private String description;

        public FailureDetail(String reason, String description) {
            this.reason = reason;
            this.description = description;
        }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public List<FailureDetail> getAllFailureReasons() { return allFailureReasons; }
    public void setAllFailureReasons(List<FailureDetail> allFailureReasons) {
        this.allFailureReasons = allFailureReasons;
    }
}