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
    @Transactional  // - ensures both operations succeed or both fail
    public Cube createCubeFromDTO(CreateCubeRequest createCubeRequest) {
        // 1. Create the cube
        Cube cube = cubeMapper.toEntity(createCubeRequest);
        cube.setDuration(durationRepo.getReferenceById(createCubeRequest.getDurationId()));
        cube.setCurrentCycle(0);
        cube.setRotationId(1);  // Set rotation system to random (1)
        
        // 2. Calculate total to be collected
        BigDecimal totalToBeCollected = createCubeRequest.getAmountPerCycle()
                .multiply(BigDecimal.valueOf(createCubeRequest.getNumberofmembers()))
                .multiply(BigDecimal.valueOf(createCubeRequest.getNumberofmembers()));
        cube.setTotalToBeCollected(totalToBeCollected);
        
        Cube savedCube = cubeRepository.save(cube);

        // 3. Add creator as admin member
        CubeMember creatorMember = new CubeMember();
        creatorMember.setCubeId(savedCube.getCubeId());
        creatorMember.setUserId(createCubeRequest.getUser_id());
        creatorMember.setRoleId(1);  // 1 = Admin role
        cubeMemberRepository.save(creatorMember);

        return savedCube;
    }

    @Override
    public List<UUID> getUserCubeIds(UUID userId) {
        List<CubeMember> membershipList = cubeMemberRepository.findByUserId(userId);
        return membershipList.stream()
                .map(CubeMember::getCubeId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cube> getAllCubes() {
        return cubeRepository.findAll();
    }

    @Override
    public Cube getCubeById(UUID cubeId) {
        return cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found with ID: " + cubeId));
    }

    @Override
    public void deleteCube(UUID cubeId) {
        cubeRepository.deleteById(cubeId);
    }
}