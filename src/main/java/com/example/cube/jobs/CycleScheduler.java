package com.example.cube.jobs;

import com.example.cube.model.Cube;
import com.example.cube.repository.CubeRepository;
import com.example.cube.service.CycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CycleScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CycleScheduler.class);

    private final CubeRepository cubeRepository;
    private final CycleService cycleService;
    // In-memory per-cube lock to avoid double-processing within a single instance
    private final Set<UUID> processingCubes = ConcurrentHashMap.newKeySet();

    @Autowired
    public CycleScheduler(CubeRepository cubeRepository, CycleService cycleService) {
        this.cubeRepository = cubeRepository;
        this.cycleService = cycleService;
    }

    /**
     * Runs every 3 mins to check for cubes ready to process
     * For testing with 1-minute cycles, 30 seconds gives quick response
     */
    @Scheduled(fixedRate = 180000)  // every 3 mins
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

            // Process each cube with per-cube locking
            for (Cube cube : readyCubes) {
                UUID cubeId = cube.getCubeId();
                // Attempt to acquire lock for this cube
                if (!processingCubes.add(cubeId)) {
                    logger.info("Scheduler: Cube {} is already being processed; skipping", cubeId);
                    continue;
                }
                try {
                    logger.info("Scheduler: Processing cycle for cube: {}", cubeId);
                    cycleService.processCycle(cubeId);
                    logger.info("Scheduler: Successfully processed cube: {}", cubeId);
                } catch (Exception e) {
                    logger.error("Scheduler: Error processing cube {}: {}", cubeId, e.getMessage(), e);
                    // Continue to next cube even if one fails
                } finally {
                    // Release lock
                    processingCubes.remove(cubeId);
                }
            }

        } catch (Exception e) {
            logger.error("Scheduler: Error in processReadyCubes: {}", e.getMessage(), e);
        }
    }
}
