package com.example.cube.repository;

import com.example.cube.model.Cube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository layer for interacting with the 'Cubes' table.
 * JpaRepository gives you built-in CRUD methods:
 * - save()
 * - findById()
 * - findAll()
 * - deleteById()
 */
@Repository
public interface CubeRepository extends JpaRepository<Cube, UUID> {
    // You can later add custom queries like:
    // List<Cube> findByUserId(UUID user_id);
}
