package com.example.cube.service;

import com.example.cube.model.Cube;
import com.example.cube.repository.CubeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private final CubeRepository cubeRepository;
    private final CycleService cycleService;

    @Autowired
    public SchedulerService(CubeRepository cubeRepository, CycleService cycleService) {
        this.cubeRepository = cubeRepository;
        this.cycleService = cycleService;
    }

    /**
     * Runs every 30 seconds to check for cubes ready to process
     * For testing with 1-minute cycles, 30 seconds gives quick response
     */
    @Scheduled(fixedRate = 60000)  // 30 seconds in milliseconds
    public void processReadyCubes() {
        logger.info("Scheduler: Checking for cubes ready to process...");

        try {
            // Find cubes where nextPayoutDate has been reached
            List<Cube> readyCubes = cubeRepository.findCubesReadyForProcessing(Instant.now());

            if (readyCubes.isEmpty()) {
                logger.debug("Scheduler: No cubes ready for processing");
                return;
            }

            logger.info("Scheduler: Found {} cube(s) ready for processing", readyCubes.size());

            // Process each cube
            for (Cube cube : readyCubes) {
                try {
                    logger.info("Scheduler: Processing cycle for cube: {}", cube.getCubeId());
                    cycleService.processCycle(cube.getCubeId());
                    logger.info("Scheduler: Successfully processed cube: {}", cube.getCubeId());
                } catch (Exception e) {
                    logger.error("Scheduler: Error processing cube {}: {}",
                            cube.getCubeId(), e.getMessage(), e);
                    // Continue to next cube even if one fails
                }
            }

        } catch (Exception e) {
            logger.error("Scheduler: Error in processReadyCubes: {}", e.getMessage(), e);
        }
    }
}