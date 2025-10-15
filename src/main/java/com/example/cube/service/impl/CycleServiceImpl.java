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
import com.example.cube.service.BankService;
import com.example.cube.service.CycleService;
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
    private final BankService bankService;

    @Autowired
    public CycleServiceImpl(CubeRepository cubeRepository,
                            CubeMemberRepository cubeMemberRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            BankService bankService) {
        this.cubeRepository = cubeRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.bankService = bankService;
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
        cube.setStartDate(Instant.now());
        cube.setNextPayoutDate(calculateNextPayoutDate(cube));

        return cubeRepository.save(cube);
    }

    @Override
    @Transactional
    public CycleProcessDTO processCycle(UUID cubeId) {

        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

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

        if (unpaidMembers.isEmpty()) {
            throw new RuntimeException("All members have been paid");
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

        // Withdraw from bank
        bankService.withdraw(payoutAmount);

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
        response.setBankBalance(bankService.getBalance());

        return response;
    }

    @Override
    public CycleStatusDTO getCurrentCycleStatus(UUID cubeId) {

        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        List<CubeMember> allMembers = cubeMemberRepository.findByCubeId(cubeId);

        // Map members to status
        List<MemberPayoutStatus> memberStatuses = allMembers.stream()
                .map(member -> {
                    MemberPayoutStatus status = new MemberPayoutStatus();
                    status.setUserId(member.getUserId());
                    status.setHasReceived(member.getHasReceivedPayout());
                    status.setPayoutCycle(member.getPayoutPosition());
                    status.setPayoutDate(member.getPayoutDate());
                    return status;
                })
                .collect(Collectors.toList());

        long unpaidCount = allMembers.stream()
                .filter(m -> !m.getHasReceivedPayout())
                .count();

        // Calculate progress percentage
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (cube.getTotalToBeCollected() != null &&
                cube.getTotalToBeCollected().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = cube.getTotalAmountCollected()
                    .divide(cube.getTotalToBeCollected(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Build response
        CycleStatusDTO response = new CycleStatusDTO();
        response.setCubeId(cubeId);
        response.setCurrentCycle(cube.getCurrentCycle());
        response.setTotalCycles(cube.getNumberofmembers());
        response.setTotalToBeCollected(cube.getTotalToBeCollected());
        response.setTotalAmountCollected(cube.getTotalAmountCollected());
        response.setProgressPercentage(progressPercentage);
        response.setNextPayoutDate(cube.getNextPayoutDate());
        response.setRemainingMembers((int) unpaidCount);
        response.setMembers(memberStatuses);
        response.setIsComplete(unpaidCount == 0);

        return response;
    }

    @Override
    @Transactional
    public boolean recordMemberPayment(UUID cubeId, UUID userId, Integer cycleNumber) {

        // 1. Get cube and member
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        CubeMember member = cubeMemberRepository.findByCubeIdAndUserId(cubeId, userId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this cube"));

        // 2. Validate cycle number
        if (!cycleNumber.equals(cube.getCurrentCycle())) {
            throw new RuntimeException("Invalid cycle number. Current cycle is: " + cube.getCurrentCycle());
        }

        // 3. Check if already paid for this cycle
        boolean alreadyPaid = paymentTransactionRepository
                .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId, member.getMemberId(), cycleNumber, 1, 2);

        if (alreadyPaid) {
            throw new RuntimeException("You have already paid for cycle " + cycleNumber);
        }

        // 4. Record the payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setCubeId(cubeId);
        transaction.setUserId(userId);
        transaction.setMemberId(member.getMemberId());
        transaction.setTypeId(1);  // contribution
        transaction.setStatusId(2);  // completed (simulated for MVP)
        transaction.setAmount(cube.getAmountPerCycle());
        transaction.setCycleNumber(cycleNumber);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        // 5. Deposit to bank
        bankService.deposit(cube.getAmountPerCycle());

        // 6. Update cube's total collected
        cube.setTotalAmountCollected(
                cube.getTotalAmountCollected().add(cube.getAmountPerCycle())
        );

        // 7. Check if all members have paid
        boolean allPaid = haveAllMembersPaid(cubeId, cycleNumber);

        // 8. If all paid and this is cycle 1, activate the cube
        if (allPaid && cycleNumber == 1 && cube.getStatusId() == 4) {
            cube.setStatusId(2);  // Set to active
        }

        cubeRepository.save(cube);

        return allPaid;
    }

    @Override
    public Map<String, Object> getCyclePaymentStatus(UUID cubeId, Integer cycleNumber) {

        List<CubeMember> allMembers = cubeMemberRepository.findByCubeId(cubeId);
        long totalMembers = allMembers.size();

        // Check payment status for each member
        List<Map<String, Object>> memberPayments = allMembers.stream()
                .map(member -> {
                    boolean hasPaid = paymentTransactionRepository
                            .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                                    cubeId, member.getMemberId(), cycleNumber, 1, 2);

                    Map<String, Object> memberInfo = new HashMap<>();
                    memberInfo.put("userId", member.getUserId());
                    memberInfo.put("memberId", member.getMemberId());
                    memberInfo.put("hasPaid", hasPaid);
                    return memberInfo;
                })
                .collect(Collectors.toList());

        long paidCount = memberPayments.stream()
                .filter(m -> (Boolean) m.get("hasPaid"))
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("cubeId", cubeId);
        result.put("cycleNumber", cycleNumber);
        result.put("totalMembers", totalMembers);
        result.put("paidMembers", paidCount);
        result.put("allPaid", paidCount >= totalMembers);
        result.put("members", memberPayments);

        return result;
    }

    // Helper method to calculate next payout date
    private Instant calculateNextPayoutDate(Cube cube) {
        if (cube.getDuration() == null || cube.getStartDate() == null) {
            return null;
        }
        int minutesToAdd = cube.getDuration().getDurationDays() * cube.getCurrentCycle();
        return cube.getStartDate().plus(minutesToAdd, ChronoUnit.MINUTES);
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