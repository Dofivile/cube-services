package com.example.cube.repository;

import com.example.cube.model.RotationSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RotationSystemRepository extends JpaRepository<RotationSystem, Integer> {
    // Basic CRUD is sufficient for lookup table
}