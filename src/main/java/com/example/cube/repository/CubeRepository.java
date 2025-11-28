package com.example.cube.repository;

import com.example.cube.model.Cube;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    @Query("SELECT c FROM Cube c WHERE c.statusId = 2 AND c.nextPayoutDate <= :now")
    List<Cube> findCubesReadyForProcessing(Instant now);

    boolean existsByInvitationCode(String invitationCode);
    Optional<Cube> findByInvitationCode(String invitationCode);
    
    // Find cubes created by user, ordered by creation date (limit 10)
    @Query("SELECT c FROM Cube c WHERE c.user_id = :userId ORDER BY c.createdAt DESC")
    List<Cube> findTop10ByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);
}
