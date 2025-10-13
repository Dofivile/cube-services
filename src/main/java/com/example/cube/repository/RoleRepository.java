package com.example.cube.repository;

import com.example.cube.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // Basic CRUD is sufficient for lookup table
}