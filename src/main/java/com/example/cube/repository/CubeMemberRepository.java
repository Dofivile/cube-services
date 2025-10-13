package com.example.cube.repository;

import com.example.cube.model.CubeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CubeMemberRepository extends JpaRepository<CubeMember, UUID> {
    List<CubeMember> findByCubeId(UUID cubeId);
    boolean existsByCubeIdAndUserId(UUID cubeId, UUID userId);
    // Find members who have or haven't received payout
    List<CubeMember> findByCubeIdAndHasReceivedPayout(UUID cubeId, Boolean hasReceivedPayout);

    // Count total members in a cube
    long countByCubeId(UUID cubeId);

    // Check if user is admin
    boolean existsByCubeIdAndUserIdAndRoleId(UUID cubeId, UUID userId, Integer roleId);

    Optional<CubeMember> findByCubeIdAndUserId(UUID cubeId, UUID userId);
}