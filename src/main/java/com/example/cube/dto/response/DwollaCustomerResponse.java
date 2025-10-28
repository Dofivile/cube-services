package com.example.cube.dto.response;

public class DwollaCustomerResponse {
    private String customerId;
    private String status;
    private String customerUrl;
    private String firstName;
    private String lastName;
    private String email;
    private String message;

    public DwollaCustomerResponse() {}

    public DwollaCustomerResponse(String customerId, String status, String customerUrl) {
        this.customerId = customerId;
        this.status = status;
        this.customerUrl = customerUrl;
    }

    // Getters and Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerUrl() { return customerUrl; }
    public void setCustomerUrl(String customerUrl) { this.customerUrl = customerUrl; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}