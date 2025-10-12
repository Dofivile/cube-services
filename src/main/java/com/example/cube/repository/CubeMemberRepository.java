package com.example.cube.repository;

import com.example.cube.model.CubeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CubeMemberRepository extends JpaRepository<CubeMember, UUID> {
    List<CubeMember> findByCubeId(UUID cubeId);
    boolean existsByCubeIdAndUserId(UUID cubeId, UUID userId);
}
