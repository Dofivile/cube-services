package com.example.cube.repository;

import com.example.cube.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionStatusRepository extends JpaRepository<TransactionStatus, Integer> {
    // Basic CRUD is sufficient for lookup table
}