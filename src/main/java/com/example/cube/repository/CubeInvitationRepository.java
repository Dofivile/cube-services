package com.example.cube.repository;

import com.example.cube.model.CubeInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface CubeInvitationRepository extends JpaRepository<CubeInvitation, UUID> {

    boolean existsByEmailAndCubeIdAndStatus(String email, UUID cubeId, String status);

}