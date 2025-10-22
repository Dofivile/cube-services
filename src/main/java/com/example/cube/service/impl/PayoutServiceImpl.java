package com.example.cube.service.impl;

import com.example.cube.model.CubeMember;
import com.example.cube.model.PaymentTransaction;
import com.example.cube.model.UserDetails;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.PaymentTransactionRepository;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.service.PayoutService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Transfer;
import com.stripe.param.TransferCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PayoutServiceImpl implements PayoutService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final UserDetailsRepository userDetailsRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final CubeMemberRepository cubeMemberRepository;

    @Autowired
    public PayoutServiceImpl(UserDetailsRepository userDetailsRepository,
                             PaymentTransactionRepository paymentTransactionRepository,
                             CubeMemberRepository cubeMemberRepository) {
        this.userDetailsRepository = userDetailsRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.cubeMemberRepository = cubeMemberRepository;
    }

    @Override
    @Transactional
    public UUID sendPayoutToWinner(UUID winnerId, BigDecimal amount, UUID cubeId, Integer cycleNumber) {

        System.out.println("\n💰 ========== SENDING PAYOUT ==========");
        System.out.println("Winner: " + winnerId);
        System.out.println("Amount: $" + amount);
        System.out.println("Cube: " + cubeId);
        System.out.println("Cycle: " + cycleNumber);

        // 1. Get winner's details
        UserDetails winner = userDetailsRepository.findById(winnerId)
                .orElseThrow(() -> new RuntimeException("Winner not found"));

        // 2. Check if winner can receive payouts
        if (!canUserReceivePayouts(winnerId)) {
            String error = "Winner hasn't completed Stripe onboarding or payouts not enabled";
            System.err.println("❌ " + error);

            // Create failed transaction record
            return createFailedTransaction(winnerId, amount, cubeId, cycleNumber, error);
        }

        // 3. Get member record
        CubeMember member = cubeMemberRepository.findByCubeIdAndUserId(cubeId, winnerId)
                .orElseThrow(() -> new RuntimeException("Member not found in cube"));

        // 4. Create Stripe Transfer
        Stripe.apiKey = stripeApiKey;

        try {
            // Convert dollars to cents
            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

            TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setDestination(winner.getStripeAccountId())
                    .setDescription("Cube payout - Cycle " + cycleNumber + " - Cube " + cubeId)
                    .build();

            Transfer transfer = Transfer.create(params);

            System.out.println("✅ Stripe Transfer Created!");
            System.out.println("   Transfer ID: " + transfer.getId());
            System.out.println("   Amount: $" + amount);
            System.out.println("   Destination: " + winner.getStripeAccountId());

            // 5. Create transaction record
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setUserId(winnerId);
            transaction.setMemberId(member.getMemberId());
            transaction.setCubeId(cubeId);
            transaction.setTypeId(2); // Type: PAYOUT
            transaction.setStatusId(2); // Status: COMPLETED
            transaction.setAmount(amount);
            transaction.setCycleNumber(cycleNumber);
            transaction.setStripeTransferId(transfer.getId());
            transaction.setProcessedAt(LocalDateTime.now());

            PaymentTransaction saved = paymentTransactionRepository.save(transaction);

            // 6. Update member payout status
            member.setHasReceivedPayout(true);
            member.setPayoutDate(LocalDateTime.now());
            cubeMemberRepository.save(member);

            System.out.println("✅ Transaction recorded: " + saved.getPaymentId());
            System.out.println("========================================\n");

            return saved.getPaymentId();

        } catch (StripeException e) {
            System.err.println("❌ Stripe transfer failed: " + e.getMessage());
            e.printStackTrace();

            // Create failed transaction record
            return createFailedTransaction(winnerId, amount, cubeId, cycleNumber, e.getMessage());
        }
    }

    @Override
    public boolean canUserReceivePayouts(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId).orElse(null);
        if (user == null || user.getStripeAccountId() == null) {
            return false;
        }

        Stripe.apiKey = stripeApiKey;
        try {
            Account acct = Account.retrieve(user.getStripeAccountId());
            boolean payoutsEnabled = acct.getPayoutsEnabled();
            boolean hasNoPendingRequirements = acct.getRequirements().getCurrentlyDue().isEmpty();

            if (!payoutsEnabled || !hasNoPendingRequirements) {
                System.out.println("⚠️ Stripe account not ready for payouts:");
                System.out.println("   payoutsEnabled=" + payoutsEnabled);
                System.out.println("   pending=" + acct.getRequirements().getCurrentlyDue());
                return false;
            }
            return true;
        } catch (StripeException e) {
            System.err.println("❌ Error checking account payout status: " + e.getMessage());
            return false;
        }
    }


    // ========== Helper Methods ==========

    private UUID createFailedTransaction(UUID winnerId, BigDecimal amount, UUID cubeId,
                                         Integer cycleNumber, String failureReason) {

        CubeMember member = cubeMemberRepository.findByCubeIdAndUserId(cubeId, winnerId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUserId(winnerId);
        transaction.setMemberId(member.getMemberId());
        transaction.setCubeId(cubeId);
        transaction.setTypeId(2); // Type: PAYOUT
        transaction.setStatusId(3); // Status: FAILED
        transaction.setAmount(amount);
        transaction.setCycleNumber(cycleNumber);
        transaction.setFailureReason(failureReason);

        PaymentTransaction saved = paymentTransactionRepository.save(transaction);

        System.out.println("❌ Failed transaction recorded: " + saved.getPaymentId());

        return saved.getPaymentId();
    }
}