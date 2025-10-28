package com.example.cube.dto.request;

import org.springframework.web.multipart.MultipartFile;

public class UploadDocumentRequest {
    private String documentType; // "passport", "license", or "idCard"
    private MultipartFile file;

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }
}