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

    @Column(name = "kycstatus")
    private String kycstatus = "pending";

    @Column(name = "kycverifiedat")
    private Instant kycverifiedat;

    @Column(name = "bankaccountverified")
    private Boolean bankaccountverified = false;

    @Column(name = "defaultpaymentmethodid", columnDefinition = "uuid")
    private UUID defaultpaymentmethodid;

    @Column(name = "phonenumber")
    private String phonenumber;

    public void setBankaccountverified(Boolean bankaccountverified) {
        this.bankaccountverified = bankaccountverified;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public void setDateofbirth(LocalDate dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public void setKycstatus(String kycstatus) {
        this.kycstatus = kycstatus;
    }

    public void setKycverifiedat(Instant kycverifiedat) {
        this.kycverifiedat = kycverifiedat;
    }

    public void setDefaultpaymentmethodid(UUID defaultpaymentmethodid) {this.defaultpaymentmethodid = defaultpaymentmethodid;}

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public LocalDate getDateofbirth() {
        return dateofbirth;
    }

    public String getKycstatus() {
        return kycstatus;
    }

    public Instant getKycverifiedat() {
        return kycverifiedat;
    }

    public Boolean getBankaccountverified() {
        return bankaccountverified;
    }

    public UUID getDefaultpaymentmethodid() {
        return defaultpaymentmethodid;
    }

    public String getPhonenumber() {
        return phonenumber;
    }
}

