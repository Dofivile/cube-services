package com.example.cube.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_details", schema = "public")
public class UserDetails {

    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID user_id;

    @Column(name = "dateofbirth")
    private LocalDate dateofbirth;

    @Column(name = "phonenumber")
    private String phonenumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "stripe_customer_id", unique = true)
    private String stripeCustomerId;

    // Add after phonenumber field:

    @Column(name = "stripe_account_id", unique = true)
    private String stripeAccountId;

    @Column(name = "stripe_payouts_enabled")
    private Boolean stripePayoutsEnabled;

    @Column(name = "dwolla_customer_id", unique = true)
    private String dwollaCustomerId;

    @Column(name = "dwolla_status")
    private String dwollaStatus;

    @Column(name = "dwolla_funding_source_id")
    private String dwollaFundingSourceId;

    public String getDwollaCustomerId() {
        return dwollaCustomerId;
    }

    public String getDwollaStatus() {
        return dwollaStatus;
    }

    public void setDwollaCustomerId(String dwollaCustomerId) {
        this.dwollaCustomerId = dwollaCustomerId;
    }

    public void setDwollaStatus(String dwollaStatus) {
        this.dwollaStatus = dwollaStatus;
    }

    public void setDwollaFundingSourceId(String dwollaFundingSourceId) {
        this.dwollaFundingSourceId = dwollaFundingSourceId;
    }

    public String getDwollaFundingSourceId() {
        return dwollaFundingSourceId;
    }

    public void setStripePayoutsEnabled(Boolean stripePayoutsEnabled) {
        this.stripePayoutsEnabled = stripePayoutsEnabled;
    }

    public Boolean getStripePayoutsEnabled() {
        return stripePayoutsEnabled;
    }
// Add getters and setters at the end:

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    // Getters and Setters

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
