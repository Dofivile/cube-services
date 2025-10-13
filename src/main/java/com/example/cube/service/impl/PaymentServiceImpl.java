package com.example.cube.service.impl;

import com.example.cube.dto.request.PaymentRequestDTO;
import com.example.cube.dto.response.PaymentResponseDTO;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.model.PaymentTransaction;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.PaymentTransactionRepository;
import com.example.cube.service.BankService;
import com.example.cube.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final CubeRepository cubeRepository;
    private final CubeMemberRepository cubeMemberRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BankService bankService;

    @Autowired
    public PaymentServiceImpl(CubeRepository cubeRepository,
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
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {

        try {
            // 1. Get cube
            Cube cube = cubeRepository.findById(request.getCubeId())
                    .orElseThrow(() -> new RuntimeException("Cube not found"));

            // 2. Get member
            CubeMember member = cubeMemberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            // 3. Validate that member belongs to this cube
            if (!member.getCubeId().equals(request.getCubeId())) {
                throw new RuntimeException("Member does not belong to this cube");
            }

            // 4. Validate that member's userId matches request
            if (!member.getUserId().equals(request.getUserId())) {
                throw new RuntimeException("User ID does not match member");
            }

            // 5. Validate cycle number
            if (!request.getCycleNumber().equals(cube.getCurrentCycle())) {
                throw new RuntimeException("Invalid cycle number. Current cycle is: " + cube.getCurrentCycle());
            }

            // 6. Check if already paid for this cycle
            boolean alreadyPaid = paymentTransactionRepository
                    .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                            request.getCubeId(),
                            request.getMemberId(),
                            request.getCycleNumber(),
                            1,  // typeId = contribution
                            2   // statusId = completed
                    );

            if (alreadyPaid) {
                throw new RuntimeException("Payment already recorded for this cycle");
            }

            // 7. Create payment transaction
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setCubeId(request.getCubeId());
            transaction.setUserId(request.getUserId());
            transaction.setMemberId(request.getMemberId());
            transaction.setTypeId(1);  // contribution
            transaction.setStatusId(2);  // completed (simulated for MVP)
            transaction.setAmount(cube.getAmountPerCycle());
            transaction.setCycleNumber(request.getCycleNumber());
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setProcessedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);

            // 8. Deposit to bank
            bankService.deposit(cube.getAmountPerCycle());

            // 9. Update cube's total collected
            cube.setTotalAmountCollected(
                    cube.getTotalAmountCollected().add(cube.getAmountPerCycle())
            );

            // 10. Check if all members have paid
            long totalMembers = cubeMemberRepository.countByCubeId(request.getCubeId());
            long paidMembers = paymentTransactionRepository
                    .countByCubeIdAndCycleNumberAndTypeIdAndStatusId(
                            request.getCubeId(),
                            request.getCycleNumber(),
                            1,  // contribution
                            2   // completed
                    );

            // 11. If all paid and this is cycle 1, activate the cube
            if (paidMembers >= totalMembers && request.getCycleNumber() == 1 && cube.getStatusId() == 4) {
                cube.setStatusId(2);  // Set to active
            }

            cubeRepository.save(cube);

            // 12. Return success response
            return new PaymentResponseDTO(true);

        } catch (Exception e) {
            // Log the error (you can add logging here)
            return new PaymentResponseDTO(false);
        }
    }
}