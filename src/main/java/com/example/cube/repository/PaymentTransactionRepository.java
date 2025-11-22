package com.example.cube.repository;

import com.example.cube.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<Transaction, UUID> {

    // Find all transactions for a specific cube and cycle
    List<Transaction> findByCubeIdAndCycleNumber(UUID cubeId, Integer cycleNumber);

    // Count contributions that are completed for a specific cycle
    long countByCubeIdAndCycleNumberAndTypeIdAndStatusId( UUID cubeId, Integer cycleNumber, Integer typeId, Integer statusId);

    // Find all transactions for a cube
    List<Transaction> findByCubeId(UUID cubeId);

    /**
     * Check if a specific member has paid for a cycle
     */
    boolean existsByCubeIdAndMemberIdAndCycleNumberAndTypeIdAndStatusId(
            UUID cubeId,
            UUID memberId,
            Integer cycleNumber,
            Integer typeId,
            Integer statusId
    );

    /**
     * Get all payment transactions for a specific cube, cycle, and type
     */
    List<Transaction> findByCubeIdAndCycleNumberAndTypeId(
            UUID cubeId,
            Integer cycleNumber,
            Integer typeId
    );

    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Get all transactions for a user, ordered by most recent first
     * Used for transaction history page
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Get recent transactions for a cube, limited to top N
     * Used for activity feed
     */
    List<Transaction> findTop20ByCubeIdOrderByCreatedAtDesc(UUID cubeId);
}