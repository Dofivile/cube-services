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
        SELECT 
            cm.member_id as memberId,
            cm.cube_id as cubeId,
            cm.user_id as userId,
            cm.role_id as roleId,
            cm.has_paid as hasPaid,
            au.email as email,
            ud.first_name as firstName,
            ud.last_name as lastName
        FROM cube_members cm
        JOIN auth.users au ON cm.user_id = au.id
        LEFT JOIN user_details ud ON cm.user_id = ud.user_id
        WHERE cm.cube_id = :cubeId
        """, nativeQuery = true)
    List<MemberWithContact> findMembersWithContactInfo(@Param("cubeId") UUID cubeId);
}