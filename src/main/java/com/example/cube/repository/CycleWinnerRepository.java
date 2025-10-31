package com.example.cube.repository;

import com.example.cube.model.CycleWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CycleWinnerRepository extends JpaRepository<CycleWinner, UUID> {

    // Check if winner already selected for a cycle
    boolean existsByCubeIdAndCycleNumber(UUID cubeId, Integer cycleNumber);

    // Get winner for a specific cycle
    Optional<CycleWinner> findByCubeIdAndCycleNumber(UUID cubeId, Integer cycleNumber);

    // Get all winners for a cube
    List<CycleWinner> findByCubeIdOrderByCycleNumberAsc(UUID cubeId);

    // Find winners that haven't been paid yet
    List<CycleWinner> findByPayoutSent(Boolean payoutSent);
}