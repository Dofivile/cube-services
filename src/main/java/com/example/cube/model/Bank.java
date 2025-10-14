package com.example.cube.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bank", schema = "public")
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_id")
    private Integer bankId;

    @Column(name = "name")
    private String name;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;


    public Integer getBankId() { return bankId;}

    public void setBankId(Integer bankId) { this.bankId = bankId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}