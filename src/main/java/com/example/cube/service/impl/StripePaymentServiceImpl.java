package com.example.cube.service.impl;

import com.example.cube.dto.response.PaymentIntentResponse;
import com.example.cube.model.*;
import com.example.cube.repository.*;
import com.example.cube.service.StripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class StripePaymentServiceImpl implements StripePaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private CubeRepository cubeRepository;

    @Autowired
    private CubeMemberRepository cubeMemberRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepositoryRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(UUID userId, UUID cubeId, UUID memberId, Integer cycleNumber) {

        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));
        CubeMember member = cubeMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getCubeId().equals(cubeId)) throw new RuntimeException("Member does not belong to this cube");
        if (!member.getUserId().equals(userId)) throw new RuntimeException("User ID does not match member");
        if (!cycleNumber.equals(cube.getCurrentCycle()))
            throw new RuntimeException("Invalid cycle number. Current cycle is: " + cube.getCurrentCycle());

        boolean alreadyPaid = paymentTransactionRepository
                .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId, memberId, cycleNumber, 1, 2);

        if (alreadyPaid) throw new RuntimeException("Payment already recorded for this cycle");

        String customerId = getOrCreateCustomer(userId);

        BigDecimal amountInDollars = cube.getAmountPerCycle();
        long amountInCents = amountInDollars.multiply(new BigDecimal("100")).longValue();

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_id", userId.toString());
            metadata.put("cube_id", cubeId.toString());
            metadata.put("member_id", memberId.toString());
            metadata.put("cycle_number", cycleNumber.toString());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setCustomer(customerId)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                    )
                                    .build()
                    )
                    .putAllMetadata(metadata)
                    .setDescription("Cube payment for " + cube.getName() + " - Cycle " + cycleNumber)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return new PaymentIntentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    customerId
            );

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void handlePaymentIntentSucceeded(String paymentIntentId) {
        try {
            // Retrieve the payment intent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Extract metadata
            Map<String, String> metadata = paymentIntent.getMetadata();
            UUID userId = UUID.fromString(metadata.get("user_id"));
            UUID cubeId = UUID.fromString(metadata.get("cube_id"));
            UUID memberId = UUID.fromString(metadata.get("member_id"));
            Integer cycleNumber = Integer.parseInt(metadata.get("cycle_number"));

            // Check if transaction already recorded
            if (paymentTransactionRepository.existsByStripePaymentIntentId(paymentIntentId)) {
                System.out.println("Payment already processed: " + paymentIntentId);
                return;
            }

            // Get cube
            Cube cube = cubeRepository.findById(cubeId)
                    .orElseThrow(() -> new RuntimeException("Cube not found"));

            // Create payment transaction record
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setUserId(userId);
            transaction.setCubeId(cubeId);
            transaction.setMemberId(memberId);
            transaction.setStripePaymentIntentId(paymentIntentId);
            transaction.setAmount(new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal("100")));
            transaction.setCycleNumber(cycleNumber);
            transaction.setTypeId(1);  // contribution
            transaction.setStatusId(2);  // completed
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setProcessedAt(LocalDateTime.now());

            paymentTransactionRepository.save(transaction);

            // Update cube's total collected
            cube.setTotalAmountCollected(
                    cube.getTotalAmountCollected().add(transaction.getAmount())
            );

            // Check if all members have paid
            long totalMembers = cubeMemberRepository.countByCubeId(cubeId);
            long paidMembers = paymentTransactionRepository
                    .countByCubeIdAndCycleNumberAndTypeIdAndStatusId(
                            cubeId, cycleNumber, 1, 2);

            // If all paid and this is cycle 1, activate the cube
            if (paidMembers >= totalMembers && cycleNumber == 1 && cube.getStatusId() == 4) {
                cube.setStatusId(2);  // Set to active
                // Set start date if not set (MVP: start when payments for cycle 1 settle)
                if (cube.getStartDate() == null) {
                    cube.setStartDate(java.time.Instant.now());
                }
                // Compute next payout date based on duration/current cycle
                if (cube.getDuration() != null && cube.getStartDate() != null) {
                    String durationName = cube.getDuration().getDurationName();
                    int durationDays = cube.getDuration().getDurationDays();
                    int current = cube.getCurrentCycle() != null ? cube.getCurrentCycle() : 1;
                    java.time.Instant next;
                    if (durationName != null && durationName.equalsIgnoreCase("MINUTES")) {
                        int minutes = 3 * current;
                        next = cube.getStartDate().plus(java.time.Duration.ofMinutes(minutes));
                    } else {
                        long days = (long) durationDays * current;
                        next = cube.getStartDate().plus(java.time.Duration.ofDays(days));
                    }
                    cube.setNextPayoutDate(next);
                }
            }

            cubeRepository.save(cube);

            System.out.println("✅ Payment processed: " + paymentIntentId +
                    " | Cube: " + cube.getName() +
                    " | Amount: $" + transaction.getAmount() +
                    " | Paid: " + paidMembers + "/" + totalMembers);

        } catch (StripeException e) {
            throw new RuntimeException("Failed to process payment intent: " + e.getMessage());
        }
    }

    private String getOrCreateCustomer(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return existing customer
        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }

        // Create new customer
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .putMetadata("user_id", userId.toString())
                    .build();

            Customer customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userDetailsRepository.save(user);

            System.out.println("✅ Created Stripe customer: " + customer.getId() + " for user: " + userId);

            return customer.getId();

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage());
        }
    }
}
