package com.example.cube.service.impl;

import com.example.cube.dto.request.CubeRequestDTO;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.DurationOptionRepository;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CubeService.
 * Handles logic between controllers and repository.
 */
@Service
public class CubeServiceImpl implements CubeService {

    private final CubeRepository cubeRepository;
    private final CubeMapper cubeMapper;

    @Autowired
    private DurationOptionRepository durationRepo;

    @Autowired
    public CubeServiceImpl(CubeRepository cubeRepository, CubeMapper cubeMapper) {
        this.cubeRepository = cubeRepository;
        this.cubeMapper = cubeMapper;
    }

    @Override
    public Cube createCubeFromDTO(CubeRequestDTO cubeRequestDTO) {
        Cube cube = cubeMapper.toEntity(cubeRequestDTO);
        cube.setDuration(durationRepo.getReferenceById(cubeRequestDTO.getDurationId()));
        return cubeRepository.save(cube);
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
