package com.example.cube.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public class SignUpAuthRequest {
    @NotBlank
    private String email;
    @NotBlank private String password;
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String phoneNumber;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
