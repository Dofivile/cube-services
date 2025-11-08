package com.example.cube.repository;

import com.example.cube.model.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalTypeRepository extends JpaRepository<GoalType, Integer> {
}