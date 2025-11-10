package com.example.cube.repository;

import com.example.cube.model.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod, UUID> {

    // Find all payment methods for a user
    List<UserPaymentMethod> findByUserId(UUID userId);

    // Find the default payment method for a user
    Optional<UserPaymentMethod> findByUserIdAndIsDefaultTrue(UUID userId);

    // Find by payment method ID
    Optional<UserPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    // Find by user and payment method ID
    Optional<UserPaymentMethod> findByUserIdAndStripePaymentMethodId(UUID userId, String stripePaymentMethodId);

    // Grab the most recently created method (used to promote new default)
    Optional<UserPaymentMethod> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

    // Check if user has any verified payment method
    boolean existsByUserIdAndBankAccountVerifiedTrue(UUID userId);
}
