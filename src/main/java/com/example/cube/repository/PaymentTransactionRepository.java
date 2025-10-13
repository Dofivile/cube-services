package com.example.cube.repository;

import com.example.cube.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    // Find all transactions for a specific cube and cycle
    List<PaymentTransaction> findByCubeIdAndCycleNumber(UUID cubeId, Integer cycleNumber);

    // Count contributions that are completed for a specific cycle
    long countByCubeIdAndCycleNumberAndTypeIdAndStatusId( UUID cubeId, Integer cycleNumber, Integer typeId, Integer statusId);

    // Find all transactions for a cube
    List<PaymentTransaction> findByCubeId(UUID cubeId);

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
    List<PaymentTransaction> findByCubeIdAndCycleNumberAndTypeId(
            UUID cubeId,
            Integer cycleNumber,
            Integer typeId
    );
}