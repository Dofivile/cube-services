package com.example.cube.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateDwollaCustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be ≤ 50 characters")
    @Pattern(regexp = "^[^<>=\"`!?%~${}\\[\\]]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be ≤ 50 characters")
    @Pattern(regexp = "^[^<>=\"`!?%~${}\\[\\]]+$", message = "Last name contains invalid characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

    private String ipAddress;

    @NotBlank(message = "Address is required")
    @Size(max = 50, message = "Address must be ≤ 50 characters")
    @Pattern(regexp = "^[^<>=\"`!?%~${}\\[\\]]+$", message = "Address contains invalid characters")
    private String address1;

    @Size(max = 50, message = "Address 2 must be ≤ 50 characters")
    @Pattern(regexp = "^[^<>=\"`!?%~${}\\[\\]]+$", message = "Address 2 contains invalid characters")
    private String address2;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must be ≤ 50 characters")
    @Pattern(regexp = "^[^<>=\"`!?%~${}\\[\\]0-9]+$", message = "City cannot contain numbers or special characters")
    private String city;

    @NotBlank(message = "State is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "State must be a valid 2-letter US state abbreviation")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Postal code must be a valid US ZIP code")
    private String postalCode;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth;

    @NotBlank(message = "SSN is required")
    @Pattern(regexp = "^\\d{4}$|^\\d{9}$", message = "SSN must be last 4 or full 9 digits")
    private String ssn;

    @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits, no separators")
    private String phone;

    @Size(max = 255, message = "Correlation ID must be ≤ 255 characters")
    @Pattern(regexp = "^[a-z0-9\\-._]*$", message = "Correlation ID can only contain: a-z, 0-9, -, ., _")
    private String correlationId;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }

    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}