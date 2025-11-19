package com.example.cube.service.impl;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.DurationOptionRepository;
import com.example.cube.repository.GoalTypeRepository;
import com.example.cube.model.GoalType;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.security.SecureRandom;

@Service
public class CubeServiceImpl implements CubeService {

    private final CubeRepository cubeRepository;
    private final CubeMapper cubeMapper;
    private final CubeMemberRepository cubeMemberRepository;

    @Autowired
    private DurationOptionRepository durationRepo;

    @Autowired
    private GoalTypeRepository goalTypeRepo;

    private static final String INVITATION_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITATION_CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public CubeServiceImpl(CubeRepository cubeRepository, CubeMapper cubeMapper, CubeMemberRepository cubeMemberRepository) {
        this.cubeRepository = cubeRepository;
        this.cubeMapper = cubeMapper;
        this.cubeMemberRepository = cubeMemberRepository;
    }

    @Override
    @Transactional
    @CacheEvict(value = "userCubes", key = "#userId")
    public Cube createCubeFromDTO(CreateCubeRequest createCubeRequest, UUID userId) {  // Add userId parameter

        Cube cube;
        Cube savedCube;
        CubeMember creatorMember;

        validateCurrency(createCubeRequest);

        cube = cubeMapper.toEntity(createCubeRequest);
        cube.setUser_id(userId);
        cube.setDuration(durationRepo.getReferenceById(createCubeRequest.getDurationId()));
        
        // Set goal type: default to "personal" (assuming ID 1) if not provided
        if (createCubeRequest.getGoalTypeId() != null) {
            GoalType goalType = goalTypeRepo.findById(createCubeRequest.getGoalTypeId())
                    .orElseThrow(() -> new RuntimeException("Goal type not found with ID: " + createCubeRequest.getGoalTypeId()));
            cube.setGoalType(goalType);
        } else {
            // Default to "personal" - you may need to adjust the ID or fetch by name
            GoalType personalGoalType = goalTypeRepo.findById(1)
                    .orElseThrow(() -> new RuntimeException("Default goal type 'personal' not found"));
            cube.setGoalType(personalGoalType);
        }
        
        // Generate unique invitation code
        cube.setInvitationCode(generateUniqueInvitationCode());
        
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

    /**
     * Generate a unique 6-character alphanumeric invitation code
     */
    private String generateUniqueInvitationCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;
        
        do {
            code = generateRandomCode();
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new RuntimeException("Unable to generate unique invitation code after " + maxAttempts + " attempts");
            }
        } while (cubeRepository.existsByInvitationCode(code));
        
        return code;
    }

    /**
     * Generate a random 6-character code
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(INVITATION_CODE_LENGTH);
        for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
            code.append(INVITATION_CODE_CHARS.charAt(random.nextInt(INVITATION_CODE_CHARS.length())));
        }
        return code.toString();
    }

    @Override
    @Cacheable(value = "userCubes", key = "#userId")
    public List<UUID> getUserCubeIds(UUID userId) {
        List<CubeMember> membershipList = cubeMemberRepository.findByUserId(userId);
        return membershipList.stream().map(CubeMember::getCubeId).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "cubeDetails", key = "#cubeId")
    public Cube getCubeById(UUID cubeId) {
        return cubeRepository.findById(cubeId).orElseThrow(() -> new RuntimeException("Cube not found with ID: " + cubeId));
    }

}
