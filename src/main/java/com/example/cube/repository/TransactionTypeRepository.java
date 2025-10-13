package com.example.cube.repository;

import com.example.cube.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, Integer> {
    // Basic CRUD is sufficient for lookup table
}