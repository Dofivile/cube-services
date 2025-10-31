package com.example.cube.service.impl;

import com.example.cube.dto.response.CycleProcessDTO;
import com.example.cube.dto.response.CycleStatusDTO;
import com.example.cube.dto.response.MemberPayoutStatus;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.model.CycleWinner;
import com.example.cube.model.PaymentTransaction;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.CycleWinnerRepository;
import com.example.cube.repository.PaymentTransactionRepository;
import com.example.cube.service.CycleService;
import com.example.cube.service.PayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CycleServiceImpl implements CycleService {

    private final CubeRepository cubeRepository;
    private final CubeMemberRepository cubeMemberRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CycleWinnerRepository cycleWinnerRepository;

    @Autowired
    public CycleServiceImpl(CubeRepository cubeRepository,
                            CubeMemberRepository cubeMemberRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            CycleWinnerRepository cycleWinnerRepository) {
        this.cubeRepository = cubeRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.cycleWinnerRepository = cycleWinnerRepository;
    }

    @Override
    @Transactional
    public Cube startCube(UUID cubeId, UUID memberId, UUID userId) {

        // 1. Get cube
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Validate member belongs to cube and matches JWT user
        CubeMember member = cubeMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        if (!member.getCubeId().equals(cubeId)) {
            throw new RuntimeException("Member does not belong to this cube");
        }
        
        if (!member.getUserId().equals(userId)) {
            throw new RuntimeException("Member does not match authenticated user");
        }

        // 3. Only allow manual start for cycle 1
        if (cube.getCurrentCycle() != 1) {
            throw new RuntimeException("Manual start only allowed for cycle 1. Subsequent cycles start automatically.");
        }

        // 4. Check if already active
        if (cube.getStatusId() == 2) {
            throw new RuntimeException("Cube is already active");
        }

        // 5. Check if user is admin (roleId = 1)
        if (member.getRoleId() != 1) {
            throw new RuntimeException("Only admin can start cube");
        }

        // 6. Check if cube is full
        long memberCount = cubeMemberRepository.countByCubeId(cubeId);
        if (memberCount < cube.getNumberofmembers()) {
            throw new RuntimeException("Cube not full yet: " + memberCount + "/" + cube.getNumberofmembers() + " members");
        }

        // 7. Check if all members have paid for cycle 1
        long paidMembers = paymentTransactionRepository
                .countByCubeIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId,
                        1,  // cycle 1
                        1,  // contribution
                        2   // completed
                );

        if (paidMembers < memberCount) {
            throw new RuntimeException("Cannot start cube: Only " + paidMembers + "/" + memberCount + " members have paid");
        }

        // 8. Activate the cube
        cube.setStatusId(2);  // active
        if (cube.getStartDate() == null) {
            cube.setStartDate(Instant.now());
        }
        cube.setNextPayoutDate(calculateNextPayoutDate(cube));

        System.out.println("✅ Cube " + cube.getName() + " manually started by admin " + userId + " (member: " + memberId + ")");

        return cubeRepository.save(cube);
    }

    @Override
    @Transactional
    public void processCycle(UUID cubeId) {

        // 1. Get cube
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Validate timing
        Instant now = Instant.now();
        if (cube.getNextPayoutDate() != null && now.isBefore(cube.getNextPayoutDate())) {
            throw new RuntimeException("Not time for payout yet. Next payout at: " + cube.getNextPayoutDate());
        }

        int currentCycle = cube.getCurrentCycle();

        // 3. Check if cube is active
        if (cube.getStatusId() != 2) {
            throw new RuntimeException("Cube must be active to process cycle. Current status: " + cube.getStatusId());
        }

        // 4. Check if winner already selected for this cycle
        if (cycleWinnerRepository.existsByCubeIdAndCycleNumber(cubeId, currentCycle)) {
            throw new RuntimeException("Winner already selected for cycle " + currentCycle);
        }

        // 5. Get unpaid members
        List<CubeMember> unpaidMembers = cubeMemberRepository
                .findByCubeIdAndHasReceivedPayout(cubeId, false);

        // Handle case where all members have been paid
        if (unpaidMembers.isEmpty()) {
            System.out.println("⚠️ Cube " + cubeId + " has all members paid. Marking as completed.");

            cube.setStatusId(3);  // completed
            cube.setEndDate(Instant.now());
            cube.setNextPayoutDate(null);
            cubeRepository.save(cube);
            return;
        }

        // 6. Select random winner
        SecureRandom random = new SecureRandom();
        CubeMember winner = unpaidMembers.get(random.nextInt(unpaidMembers.size()));

        // 7. Calculate payout amount
        BigDecimal payoutAmount = cube.getAmountPerCycle()
                .multiply(BigDecimal.valueOf(cube.getNumberofmembers()));

        // 8. Record winner in cycle_winners table
        CycleWinner cycleWinner = new CycleWinner();
        cycleWinner.setCubeId(cubeId);
        cycleWinner.setMemberId(winner.getMemberId());
        cycleWinner.setUserId(winner.getUserId());
        cycleWinner.setCycleNumber(currentCycle);
        cycleWinner.setPayoutAmount(payoutAmount);
        cycleWinner.setSelectedAt(LocalDateTime.now());
        cycleWinner.setPayoutSent(false);

        cycleWinnerRepository.save(cycleWinner);

        System.out.println("✅ Winner selected for cycle " + currentCycle);
        System.out.println("   Cube: " + cubeId);
        System.out.println("   Winner: " + winner.getUserId());
        System.out.println("   Amount: $" + payoutAmount);
    }

    private Instant calculateNextPayoutDate(Cube cube) {
        if (cube.getDuration() == null || cube.getStartDate() == null) {
            return null;
        }

        String durationName = cube.getDuration().getDurationName();
        int durationDays = cube.getDuration().getDurationDays();
        int current = cube.getCurrentCycle() != null ? cube.getCurrentCycle() : 1;

        // Testing shortcut: if duration name indicates 3-minute cycles, advance in minutes
        if (durationName != null && durationName.equalsIgnoreCase("MINUTES")) {
            int minutes = 3 * current;
            return cube.getStartDate().plus(minutes, ChronoUnit.MINUTES);
        }

        long days = (long) durationDays * current;
        return cube.getStartDate().plus(days, ChronoUnit.DAYS);
    }

    // Helper method to check if all members have paid for a cycle
    private boolean haveAllMembersPaid(UUID cubeId, Integer cycleNumber) {
        // Get total number of members in the cube
        long totalMembers = cubeMemberRepository.countByCubeId(cubeId);

        // Count how many members have completed payment for this cycle
        long paidMembers = paymentTransactionRepository
                .countByCubeIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId,
                        cycleNumber -1,
                        1,  // typeId = 1 (contribution)
                        2   // statusId = 2 (completed)
                );

        return paidMembers >= totalMembers;
    }
}
