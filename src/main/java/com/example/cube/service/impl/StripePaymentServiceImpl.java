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
import com.stripe.param.CustomerUpdateParams;
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

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(UUID userId, UUID cubeId, UUID memberId, Integer cycleNumber) {

        // Validate cube exists
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // Validate member exists
        CubeMember member = cubeMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Validate member belongs to cube and matches user
        if (!member.getCubeId().equals(cubeId)) {
            throw new RuntimeException("Member does not belong to this cube");
        }
        if (!member.getUserId().equals(userId)) {
            throw new RuntimeException("User ID does not match member");
        }
        if (!cycleNumber.equals(cube.getCurrentCycle())) {
            throw new RuntimeException("Invalid cycle number. Current cycle is: " + cube.getCurrentCycle());
        }

        // Check if payment already made for this cycle
        boolean alreadyPaid = paymentTransactionRepository
                .existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
                        cubeId, memberId, cycleNumber, 1, 2);

        if (alreadyPaid) {
            throw new RuntimeException("Payment already recorded for this cycle");
        }

        // Get or create Stripe customer
        String customerId = getOrCreateCustomer(userId);

        // Calculate amount in cents
        BigDecimal amountInDollars = cube.getAmountPerCycle();
        long amountInCents = amountInDollars.multiply(new BigDecimal("100")).longValue();

        try {
            // Build metadata for tracking
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_id", userId.toString());
            metadata.put("cube_id", cubeId.toString());
            metadata.put("member_id", memberId.toString());
            metadata.put("cycle_number", cycleNumber.toString());
            metadata.put("payment_type", "card");

            // Create PaymentIntent for card payments with automatic payment methods
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("usd")
                    .setCustomer(customerId)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION)
                    .putAllMetadata(metadata)
                    .setDescription("Cube payment for " + cube.getName() + " - Cycle " + cycleNumber)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            System.out.println("✅ Card PaymentIntent created: " + paymentIntent.getId());
            System.out.println("   Status: " + paymentIntent.getStatus());
            System.out.println("   Amount: $" + (amountInCents / 100.0));

            return new PaymentIntentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    customerId
            );

        } catch (StripeException e) {
            System.err.println("❌ Failed to create card payment intent: " + e.getMessage());
            throw new RuntimeException("Failed to create card payment intent: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handlePaymentIntentSucceeded(String paymentIntentId) {
        try {
            // Retrieve the payment intent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Check if transaction already recorded
            if (paymentTransactionRepository.existsByStripePaymentIntentId(paymentIntentId)) {
                System.out.println("⚠️ Card payment already processed: " + paymentIntentId);
                return;
            }

            // Extract and validate metadata
            Map<String, String> metadata = paymentIntent.getMetadata();

            if (metadata == null || metadata.isEmpty()) {
                System.err.println("❌ PaymentIntent " + paymentIntentId + " has no metadata");
                return;
            }

            // Validate and parse metadata
            if (!hasRequiredMetadata(metadata)) {
                System.err.println("❌ Missing required metadata in " + paymentIntentId);
                return;
            }

            UUID userId = UUID.fromString(metadata.get("user_id"));
            UUID cubeId = UUID.fromString(metadata.get("cube_id"));
            UUID memberId = UUID.fromString(metadata.get("member_id"));
            Integer cycleNumber = Integer.parseInt(metadata.get("cycle_number"));

            // Get cube
            Cube cube = cubeRepository.findById(cubeId)
                    .orElseThrow(() -> new RuntimeException("Cube not found: " + cubeId));

            // Create payment transaction record
            Transaction transaction = new Transaction();
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
            cubeRepository.save(cube);

            // ✅ Update member status to "paid" (status_id = 2)
            CubeMember member = cubeMemberRepository.findById(memberId)
                    .orElse(null);
            if (member != null) {
                member.setStatusId(2);  // 2 = "paid"
                cubeMemberRepository.save(member);
                System.out.println("✅ Member " + memberId + " marked as PAID for cycle " + cycleNumber);
            }

            System.out.println("✅ Card payment recorded: " + paymentIntentId +
                    " | Cube: " + cube.getName() +
                    " | Amount: $" + transaction.getAmount() +
                    " | Cycle: " + cycleNumber);

        } catch (Exception e) {
            System.err.println("❌ Error processing card payment: " + e.getMessage());
            throw new RuntimeException("Failed to process card payment: " + e.getMessage());
        }
    }

    private boolean hasRequiredMetadata(Map<String, String> metadata) {
        return metadata.containsKey("user_id") &&
                metadata.containsKey("cube_id") &&
                metadata.containsKey("member_id") &&
                metadata.containsKey("cycle_number");
    }

    private String getOrCreateCustomer(UUID userId) {
        UserDetails user = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStripeCustomerId() != null) {
            try {
                Customer customer = Customer.retrieve(user.getStripeCustomerId());
                String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " " +
                        (user.getLastName() != null ? user.getLastName() : "")).trim();

                if (!fullName.isEmpty() && (customer.getName() == null || customer.getName().isBlank())) {
                    CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
                            .setName(fullName)
                            .build();
                    customer.update(updateParams);
                    System.out.println("✅ Updated Stripe customer name: " + fullName);
                }
            } catch (StripeException e) {
                System.err.println("⚠️ Failed to update customer name: " + e.getMessage());
            }
            return user.getStripeCustomerId();
        }


        // Create new customer
        try {
            // Build customer name
            String customerName = null;
            if (user.getFirstName() != null || user.getLastName() != null) {
                customerName = (user.getFirstName() != null ? user.getFirstName() : "") +
                        " " +
                        (user.getLastName() != null ? user.getLastName() : "");
                customerName = customerName.trim();
            }

            CustomerCreateParams.Builder paramsBuilder = CustomerCreateParams.builder()
                    .putMetadata("user_id", userId.toString());

            // Add name if available
            if (customerName != null && !customerName.isEmpty()) {
                paramsBuilder.setName(customerName);
            }

            CustomerCreateParams params = paramsBuilder.build();
            Customer customer = Customer.create(params);

            user.setStripeCustomerId(customer.getId());
            userDetailsRepository.save(user);

            System.out.println("✅ Created Stripe customer: " + customer.getId() + " (Name: " + customerName);

            return customer.getId();

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage());
        }
    }
}