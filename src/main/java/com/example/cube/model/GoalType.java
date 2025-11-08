package com.example.cube.model;

import jakarta.persistence.*;

@Entity
@Table(name = "goal_type", schema = "public")
public class GoalType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_type_id")
    private Integer goalTypeId;

    @Column(name = "goal_type_name", nullable = false, unique = true)
    private String goalTypeName;

    // Getters and Setters
    public Integer getGoalTypeId() {
        return goalTypeId;
    }

    public void setGoalTypeId(Integer goalTypeId) {
        this.goalTypeId = goalTypeId;
    }

    public String getGoalTypeName() {
        return goalTypeName;
    }

    public void setGoalTypeName(String goalTypeName) {
        this.goalTypeName = goalTypeName;
    }
}