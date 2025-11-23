package com.example.cube.service.impl;

import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.model.CycleWinner;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.CycleWinnerRepository;
import com.example.cube.repository.PaymentTransactionRepository;
import com.example.cube.service.CycleService;
import com.example.cube.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CycleServiceImpl implements CycleService {

    private final CubeRepository cubeRepository;
    private final CubeMemberRepository cubeMemberRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CycleWinnerRepository cycleWinnerRepository;
    private final EmailService emailService;

    @Autowired
    public CycleServiceImpl(CubeRepository cubeRepository,
                            CubeMemberRepository cubeMemberRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            CycleWinnerRepository cycleWinnerRepository,
                            EmailService emailService) {
        this.cubeRepository = cubeRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.cycleWinnerRepository = cycleWinnerRepository;
        this.emailService = emailService;
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

        // 5. Verify all members have paid for current cycle before selecting winner
        if (!haveAllMembersPaid(cubeId, currentCycle)) {
            System.out.println("⚠️ Not all members have paid for cycle " + currentCycle + ". Skipping winner selection.");
            return;  // Exit early, don't select winner yet
        }

        // 6. Get all members
        List<CubeMember> allMembers = cubeMemberRepository.findByCubeId(cubeId);

        // 7. Get members who have already won
        List<CycleWinner> winners = cycleWinnerRepository.findByCubeIdOrderByCycleNumberAsc(cubeId);
        Set<UUID> winnerUserIds = winners.stream()
                .map(CycleWinner::getUserId)
                .collect(Collectors.toSet());

        // 8. Find members who haven't won yet
        List<CubeMember> eligibleMembers = allMembers.stream()
                .filter(member -> !winnerUserIds.contains(member.getUserId()))
                .collect(Collectors.toList());

        // Safety check: if all members have won (shouldn't happen with new logic, but defensive)
        if (eligibleMembers.isEmpty()) {
            System.out.println("✅ All members have won their cycles. Completing cube (safety check).");
            cube.setStatusId(3);  // completed
            cube.setEndDate(Instant.now());
            cube.setNextPayoutDate(null);
            cubeRepository.save(cube);
            return;
        }

        // 9. Select random winner from eligible members
        SecureRandom random = new SecureRandom();
        CubeMember winner = eligibleMembers.get(random.nextInt(eligibleMembers.size()));

        // 10. Calculate payout amount
        BigDecimal payoutAmount = cube.getAmountPerCycle()
                .multiply(BigDecimal.valueOf(cube.getNumberofmembers()));

        // 11. Record winner in cycle_winners table
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

        // Check if this was the last member to win
        List<CycleWinner> allWinnersNow = cycleWinnerRepository.findByCubeIdOrderByCycleNumberAsc(cubeId);
        Set<UUID> allWinnerUserIds = allWinnersNow.stream()
                .map(CycleWinner::getUserId)
                .collect(Collectors.toSet());

        // Send notification emails
        try {
            emailService.sendWinnerNotificationEmails(cube, winner, payoutAmount, currentCycle);
        } catch (Exception e) {
            // Log error but don't fail the transaction - winner is already recorded
            System.err.println("⚠️ Failed to send notification emails, but cycle processing completed: " + e.getMessage());
        }

        if (allWinnerUserIds.size() >= allMembers.size()) {
            // All members have won - complete the cube now
            System.out.println("✅ All members have won their cycles. Completing cube after cycle " + currentCycle + ".");
            cube.setStatusId(3);  // completed
            cube.setEndDate(Instant.now());
            cube.setNextPayoutDate(null);
            cubeRepository.save(cube);
            return;  // Exit without incrementing cycle
        }

        // Still more members to win - prepare for next cycle
        System.out.println("📊 Cycle " + currentCycle + " complete. Preparing for cycle " + (currentCycle + 1));
        cube.setCurrentCycle(currentCycle + 1);
        cube.setNextPayoutDate(calculateNextPayoutDate(cube));
        cubeRepository.save(cube);

        // Reset member payment statuses for next cycle
        resetMemberPaymentStatuses(cubeId);
    }

    private Instant calculateNextPayoutDate(Cube cube) {
        if (cube.getDuration() == null || cube.getStartDate() == null) {
            return null;
        }

        String durationName = cube.getDuration().getDurationName();
        int durationDays = cube.getDuration().getDurationDays();
        int current = cube.getCurrentCycle() != null ? cube.getCurrentCycle() : 1;

        // Testing shortcut: if duration name indicates 3-minute cycles, advance in minutes/ remove in prod
        if (durationName != null && durationName.equalsIgnoreCase("MINUTES")) {
            int minutes = 3 * current;
            return cube.getStartDate().plus(minutes, ChronoUnit.MINUTES);
        }

        long days = (long) durationDays * current;
        return cube.getStartDate().plus(days, ChronoUnit.DAYS);
    }

    // Helper method to check if all members have paid for a cycle
    private boolean haveAllMembersPaid(UUID cubeId, Integer cycleNumber) {
        // Get all members in the cube
        List<CubeMember> members = cubeMemberRepository.findByCubeId(cubeId);
        
        if (members.isEmpty()) {
            return false;
        }

        // Check if all members have status_id = 2 (paid)
        boolean allPaid = members.stream()
                .allMatch(m -> m.getStatusId() != null && m.getStatusId() == 2);
        
        if (!allPaid) {
            long paidCount = members.stream()
                    .filter(m -> m.getStatusId() != null && m.getStatusId() == 2)
                    .count();
            System.out.println("   Payment status: " + paidCount + "/" + members.size() + " members have paid");
        }
        
        return allPaid;
    }

    // ✅ ADD: Helper method to reset all member payment statuses
    private void resetMemberPaymentStatuses(UUID cubeId) {
        List<CubeMember> members = cubeMemberRepository.findByCubeId(cubeId);
        
        for (CubeMember member : members) {
            member.setStatusId(1);  // Reset to "has not paid"
        }
        
        cubeMemberRepository.saveAll(members);
        System.out.println("✅ Reset payment status to 'has not paid' for " + members.size() + " members");
    }
}