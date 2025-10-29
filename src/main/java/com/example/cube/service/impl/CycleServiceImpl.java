package com.example.cube.service.impl;

import com.example.cube.dto.response.CycleProcessDTO;
import com.example.cube.dto.response.CycleStatusDTO;
import com.example.cube.dto.response.MemberPayoutStatus;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.model.PaymentTransaction;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
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
    private final PayoutService payoutService;

    @Autowired
    public CycleServiceImpl(CubeRepository cubeRepository,
                            CubeMemberRepository cubeMemberRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            PayoutService payoutService) {
        this.cubeRepository = cubeRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.payoutService = payoutService;
    }

    @Override
    @Transactional
    public Cube startCube(UUID cubeId, UUID userId) {

        // 1. Get cube
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Check if user is admin
        boolean isAdmin = cubeMemberRepository.existsByCubeIdAndUserIdAndRoleId(cubeId, userId, 1);
        if (!isAdmin) {
            throw new RuntimeException("Only admin can start cube");
        }

        // 3. Check if cube is full
        long memberCount = cubeMemberRepository.countByCubeId(cubeId);
        if (memberCount < cube.getNumberofmembers()) {
            throw new RuntimeException("Cube not full yet: " + memberCount + "/" + cube.getNumberofmembers() + " members");
        }

        // 5. Check if all members have paid for cycle 1
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

        // 6. Activate the cube
        cube.setStatusId(2);  // active
        // Only set startDate if not already provided
        if (cube.getStartDate() == null) {
            cube.setStartDate(Instant.now());
        }
        cube.setNextPayoutDate(calculateNextPayoutDate(cube));

        return cubeRepository.save(cube);
    }

    @Override
    @Transactional
    public CycleProcessDTO processCycle(UUID cubeId) {

        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // Time-gate: only process when payout time has been reached
        Instant now = Instant.now();
        if (cube.getNextPayoutDate() != null && now.isBefore(cube.getNextPayoutDate())) {
            throw new RuntimeException("Not time for payout yet. Next payout at: " + cube.getNextPayoutDate());
        }

        int currentCycle = cube.getCurrentCycle();

        // Check if cube is active
        if (cube.getStatusId() != 2) {
            throw new RuntimeException("Cube must be active to process cycle. Current status: " + cube.getStatusId());
        }

        // 1. Get all members (payments already recorded via recordMemberPayment)
        List<CubeMember> allMembers = cubeMemberRepository.findByCubeId(cubeId);

        // 2. Select random winner from unpaid members
        List<CubeMember> unpaidMembers = cubeMemberRepository
                .findByCubeIdAndHasReceivedPayout(cubeId, false);

        // FIXED: Handle the case where all members have already been paid
        if (unpaidMembers.isEmpty()) {
            System.out.println("⚠️ Cube " + cubeId + " has all members paid. Marking as completed.");

            // Mark cube as completed if not already
            if (cube.getStatusId() == 2) {
                cube.setStatusId(3);  // completed
                cube.setEndDate(Instant.now());
                cube.setNextPayoutDate(null);
                cubeRepository.save(cube);
            }

            // Return a response indicating completion
            CycleProcessDTO response = new CycleProcessDTO();
            response.setCycle(currentCycle);
            response.setWinnerUserId(null);
            response.setPayoutAmount(BigDecimal.ZERO);
            response.setRemainingMembers(0);
            response.setIsComplete(true);
            return response;
        }

        SecureRandom random = new SecureRandom();
        CubeMember winner = unpaidMembers.get(random.nextInt(unpaidMembers.size()));

        // 3. Calculate and process payout
        BigDecimal payoutAmount = cube.getAmountPerCycle()
                .multiply(BigDecimal.valueOf(cube.getNumberofmembers()));

        // Create payout transaction
        PaymentTransaction payoutTx = new PaymentTransaction();
        payoutTx.setCubeId(cubeId);
        payoutTx.setUserId(winner.getUserId());
        payoutTx.setMemberId(winner.getMemberId());
        payoutTx.setTypeId(2);  // payout
        payoutTx.setStatusId(2);  // completed
        payoutTx.setAmount(payoutAmount);
        payoutTx.setCycleNumber(currentCycle);
        payoutTx.setCreatedAt(LocalDateTime.now());
        payoutTx.setProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(payoutTx);

        // Funds movement handled via Stripe Transfer/Payout (no local bank withdrawal)

        // Mark winner as paid
        winner.setHasReceivedPayout(true);
        winner.setPayoutDate(LocalDateTime.now());
        winner.setPayoutPosition(currentCycle);
        cubeMemberRepository.save(winner);

        // 4. Check if cube is complete
        boolean isComplete = unpaidMembers.size() == 1;  // This was the last one

        if (isComplete) {
            cube.setStatusId(3);  // completed
            cube.setEndDate(Instant.now());
            cube.setNextPayoutDate(null);
            System.out.println("✅ Cube " + cubeId + " completed! All members have received payouts.");
        } else {
            // Advance to next cycle
            cube.setCurrentCycle(currentCycle + 1);
            cube.setNextPayoutDate(calculateNextPayoutDate(cube));
        }

        cubeRepository.save(cube);

        // 5. Build response
        CycleProcessDTO response = new CycleProcessDTO();
        response.setCycle(currentCycle);
        response.setWinnerUserId(winner.getUserId());
        response.setPayoutAmount(payoutAmount);
        response.setRemainingMembers(unpaidMembers.size() - 1);
        response.setIsComplete(isComplete);

        return response;
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
