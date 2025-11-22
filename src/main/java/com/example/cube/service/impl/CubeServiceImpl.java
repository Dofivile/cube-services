package com.example.cube.service.impl;

import com.example.cube.dto.request.CreateCubeRequest;
import com.example.cube.dto.response.CubeActivityResponse;
import com.example.cube.mapper.CubeMapper;
import com.example.cube.model.*;
import com.example.cube.repository.*;
import com.example.cube.service.CubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    @Autowired
    private CycleWinnerRepository cycleWinnerRepository;
    
    @Autowired
    private UserDetailsRepository userDetailsRepository;

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
    public List<UUID> getUserCubeIds(UUID userId) {
        List<CubeMember> membershipList = cubeMemberRepository.findByUserId(userId);
        return membershipList.stream().map(CubeMember::getCubeId).collect(Collectors.toList());
    }

    @Override
    public Cube getCubeById(UUID cubeId) {
        return cubeRepository.findById(cubeId).orElseThrow(() -> new RuntimeException("Cube not found with ID: " + cubeId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CubeActivityResponse> getCubeActivity(UUID cubeId, int limit) {
        List<CubeActivityResponse> activities = new ArrayList<>();
        
        // 1. Get recent payments (transactions)
        List<Transaction> payments = paymentTransactionRepository.findTop20ByCubeIdOrderByCreatedAtDesc(cubeId);
        for (Transaction payment : payments) {
            CubeActivityResponse activity = new CubeActivityResponse();
            activity.setActivityType("PAYMENT");
            activity.setUserId(payment.getUserId());
            activity.setTimestamp(payment.getCreatedAt());
            activity.setAmount(payment.getAmount());
            activity.setCycleNumber(payment.getCycleNumber());
            activity.setColorCode("green");
            
            // Get user name
            String userName = getUserName(payment.getUserId());
            activity.setUserName(userName);
            activity.setActivityText(userName + " contributed to pool");
            
            activities.add(activity);
        }
        
        // 2. Get recent winners
        List<CycleWinner> winners = cycleWinnerRepository.findTop10ByCubeIdOrderBySelectedAtDesc(cubeId);
        for (CycleWinner winner : winners) {
            CubeActivityResponse activity = new CubeActivityResponse();
            activity.setActivityType("WINNER");
            activity.setUserId(winner.getUserId());
            activity.setTimestamp(winner.getSelectedAt());
            activity.setAmount(winner.getPayoutAmount());
            activity.setCycleNumber(winner.getCycleNumber());
            activity.setColorCode("yellow");
            
            String userName = getUserName(winner.getUserId());
            activity.setUserName(userName);
            activity.setActivityText(userName + " won round " + winner.getCycleNumber());
            
            activities.add(activity);
        }
        
        // 3. Get recent member joins
        List<CubeMember> members = cubeMemberRepository.findTop20ByCubeIdOrderByJoinedAtDesc(cubeId);
        for (CubeMember member : members) {
            CubeActivityResponse activity = new CubeActivityResponse();
            activity.setActivityType("MEMBER_JOIN");
            activity.setUserId(member.getUserId());
            activity.setTimestamp(member.getJoinedAt());
            activity.setColorCode("blue");
            
            String userName = getUserName(member.getUserId());
            activity.setUserName(userName);
            activity.setActivityText(userName + " joined the cube");
            
            activities.add(activity);
        }
        
        // 4. Sort by timestamp (newest first) and limit
        return activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Helper method to get user display name
     */
    private String getUserName(UUID userId) {
        return userDetailsRepository.findById(userId)
                .map(u -> {
                    String first = u.getFirstName();
                    String last = u.getLastName();
                    if (first != null && last != null && !last.isEmpty()) {
                        return first + " " + last.substring(0, 1) + ".";
                    } else if (first != null) {
                        return first;
                    }
                    return "User";
                })
                .orElse("Unknown User");
    }

}
