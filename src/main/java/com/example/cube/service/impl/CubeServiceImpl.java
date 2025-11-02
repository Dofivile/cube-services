package com.example.cube.service.impl;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.DurationOptionRepository;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CubeServiceImpl implements CubeService {

    private final CubeRepository cubeRepository;
    private final CubeMapper cubeMapper;
    private final CubeMemberRepository cubeMemberRepository;

    @Autowired
    private DurationOptionRepository durationRepo;

    @Autowired
    public CubeServiceImpl(CubeRepository cubeRepository, CubeMapper cubeMapper, CubeMemberRepository cubeMemberRepository) {
        this.cubeRepository = cubeRepository;
        this.cubeMapper = cubeMapper;
        this.cubeMemberRepository = cubeMemberRepository;
    }

    @Override
    @Transactional
    public Cube createCubeFromDTO(CreateCubeRequest createCubeRequest, UUID userId) {  // Add userId parameter

        Cube cube;
        Cube savedCube;
        CubeMember creatorMember;

        validateCurrency(createCubeRequest);

        cube = cubeMapper.toEntity(createCubeRequest);
        cube.setUser_id(userId);
        cube.setDuration(durationRepo.getReferenceById(createCubeRequest.getDurationId()));
        cube.setCurrentCycle(1);
        cube.setRotationId(1);
        savedCube = cubeRepository.save(cube);

        // Add creator as admin member
        creatorMember = new CubeMember();
        creatorMember.setCubeId(savedCube.getCubeId());
        creatorMember.setUserId(userId);  // ✅ Use the userId parameter from auth token
        creatorMember.setRoleId(1);  // 1 = Admin role
        cubeMemberRepository.save(creatorMember);

        return savedCube;
    }

    private void validateCurrency(CreateCubeRequest req) {
        // Start/end dates are set automatically when cube starts
        String currency = req.getCurrency();
        if (currency == null || !currency.matches("[A-Z]{3}")) {
            throw new RuntimeException("currency must be a 3-letter uppercase code (e.g., USD)");
        }
        if (!"USD".equals(currency)) {
            throw new RuntimeException("Only USD is supported at this time");
        }
    }

    @Override
    public List<UUID> getUserCubeIds(UUID userId) {
        List<CubeMember> membershipList = cubeMemberRepository.findByUserId(userId);
        return membershipList.stream().map(CubeMember::getCubeId).collect(Collectors.toList());
    }

    @Override
    public Cube getCubeById(UUID cubeId) {
        return cubeRepository.findById(cubeId).orElseThrow(() -> new RuntimeException("Cube not found with ID: " + cubeId));
    }

}
