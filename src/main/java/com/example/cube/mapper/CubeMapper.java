package com.example.cube.mapper;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.dto.response.CreateCubeResponse;
import com.example.cube.dto.response.GetCubeResponse;
import com.example.cube.dto.response.StartCubeResponse;
import com.example.cube.model.Cube;
import com.example.cube.model.DurationOption;
import org.springframework.stereotype.Component;

/**
 * Handles conversions between Cube entities, request DTOs, and response DTOs.
 */
@Component
public class CubeMapper {

    // Request → Entity
    public Cube toEntity(CreateCubeRequest dto) {
        Cube cube = new Cube();
        DurationOption duration = new DurationOption(); // temporary placeholder

        duration.setDurationId(dto.getDurationId());
        cube.setName(dto.getName());
        cube.setDescription(dto.getDescription());
        cube.setUser_id(dto.getUser_id());
        cube.setAmountPerCycle(dto.getAmountPerCycle());
        cube.setNumberofmembers(dto.getNumberofmembers());
        cube.setCurrency(dto.getCurrency());
        cube.setDuration(duration);
        return cube;
    }

    // Entity → Response
    public CreateCubeResponse toResponse(Cube cube){
        CreateCubeResponse res = new CreateCubeResponse();
        res.setCubeId(cube.getCubeId());
        res.setName(cube.getName());
        res.setDescription(cube.getDescription());
        res.setUser_id(cube.getUser_id());
        res.setAmountPerCycle(cube.getAmountPerCycle());
        res.setNumberofmembers(cube.getNumberofmembers());
        res.setCurrency(cube.getCurrency());
        res.setStartDate(cube.getStartDate());
        res.setEndDate(cube.getEndDate());
        res.setNextPayoutDate(cube.getNextPayoutDate());
        // Compute total to be collected on demand
        res.setTotalToBeCollected(computeTotalToBeCollected(cube));
        res.setCreatedAt(cube.getCreatedAt());

        if (cube.getDuration() != null) {
            res.setDurationId(cube.getDuration().getDurationId());
        }
        return res;
    }

    // Entity → StartCubeResponse
    public StartCubeResponse toStartCubeResponse(Cube cube) {
        return new StartCubeResponse(
                cube.getCubeId(),
                cube.getStatusId(),
                cube.getCurrentCycle(),
                cube.getStartDate(),
                cube.getEndDate(),
                computeTotalToBeCollected(cube)
        );
    }

    public GetCubeResponse toGetCubeResponse(Cube cube) {
        GetCubeResponse response = new GetCubeResponse();
        response.setCubeId(cube.getCubeId());
        response.setUserId(cube.getUser_id());
        response.setName(cube.getName());
        response.setDescription(cube.getDescription());
        response.setAmountPerCycle(cube.getAmountPerCycle());
        response.setNextPayoutDate(cube.getNextPayoutDate());
        response.setCurrentCycle(cube.getCurrentCycle());
        response.setCurrency(cube.getCurrency());
        response.setNumberOfMembers(cube.getNumberofmembers());
        response.setStartDate(cube.getStartDate());
        response.setEndDate(cube.getEndDate());

        // Add contribution frequency from duration
        if (cube.getDuration() != null) {
            response.setContributionFrequency(cube.getDuration().getDurationName());
            response.setContributionFrequencyDays(cube.getDuration().getDurationDays());
        }

        return response;
    }

    private java.math.BigDecimal computeTotalToBeCollected(Cube cube) {
        if (cube == null || cube.getAmountPerCycle() == null || cube.getNumberofmembers() == null) {
            return java.math.BigDecimal.ZERO;
        }
        return cube.getAmountPerCycle()
                .multiply(java.math.BigDecimal.valueOf(cube.getNumberofmembers()))
                .multiply(java.math.BigDecimal.valueOf(cube.getNumberofmembers()));
    }
}
