package com.example.cube.repository;

import com.example.cube.model.CubeInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CubeInvitationRepository extends JpaRepository<CubeInvitation, UUID> {

    boolean existsByEmailAndCubeIdAndStatus(String email, UUID cubeId, String status);
    boolean existsByEmailAndCubeIdAndStatusId(String email, UUID cubeId, Integer statusId);
    Optional<CubeInvitation> findByInviteToken(String inviteToken);
    List<CubeInvitation> findByCubeIdAndStatus(UUID cubeId, String status);
    List<CubeInvitation> findByInviteeIdAndStatus(UUID inviteeId, String status);
    List<CubeInvitation> findByEmailAndStatus(String email, String status);

}