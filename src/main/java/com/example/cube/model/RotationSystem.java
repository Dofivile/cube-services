package com.example.cube.model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"RotationSystem\"", schema = "public")
public class RotationSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rotation_id")
    private Integer rotationId;

    @Column(name = "rotation_name", nullable = false, unique = true)
    private String rotationName;



    // Getters and Setters
    public Integer getRotationId() {
        return rotationId;
    }

    public void setRotationId(Integer rotationId) {
        this.rotationId = rotationId;
    }

    public String getRotationName() {
        return rotationName;
    }

    public void setRotationName(String rotationName) {
        this.rotationName = rotationName;
    }
}