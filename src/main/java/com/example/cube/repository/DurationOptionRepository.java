package com.example.cube.repository;

import com.example.cube.model.DurationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DurationOptionRepository extends JpaRepository<DurationOption, Integer> {
}
