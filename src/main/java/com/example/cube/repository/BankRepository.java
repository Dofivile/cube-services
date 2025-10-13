package com.example.cube.repository;

import com.example.cube.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
    // No custom methods needed for MVP
    // Basic CRUD from JpaRepository is sufficient
}