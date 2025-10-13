package com.example.cube.repository;

import com.example.cube.model.CubeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CubeStatusRepository extends JpaRepository<CubeStatus, Integer> {
    // Basic CRUD is sufficient for lookup table
}