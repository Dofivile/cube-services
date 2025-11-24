package com.example.cube.repository;

import com.example.cube.dto.MemberWithContact;
import com.example.cube.model.CubeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<CubeMember> findByUserId(UUID userId);

    /**
     * Get cube members with their contact information (email, name) in one query
     * Joins with auth.users and user_details tables
     */
    @Query(value = """
    SELECT * FROM vw_member_contacts
    WHERE cube_id = :cubeId
    """, nativeQuery = true)
    List<MemberWithContact> findMembersWithContactInfo(@Param("cubeId") UUID cubeId);

    /**
     * Get recent members who joined a cube, ordered by most recent first
     * Used for activity feed
     */
    List<CubeMember> findTop20ByCubeIdOrderByJoinedAtDesc(UUID cubeId);
    
    /**
     * Get recent cubes joined by user, ordered by most recent first
     * Used for user activity feed
     */
    List<CubeMember> findTop20ByUserIdOrderByJoinedAtDesc(UUID userId);

}