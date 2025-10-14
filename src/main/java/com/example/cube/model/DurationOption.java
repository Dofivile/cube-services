package com.example.cube.model;

import jakarta.persistence.*;

@Entity
@Table(name = "duration", schema = "public")
public class DurationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duration_id")
    private Integer durationId;

    @Column(name = "duration_name", nullable = false, unique = true)
    private String durationName; // daily, weekly, monthly

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays; // 1, 7, 30

    public void setDurationId(Integer durationId) {
        this.durationId = durationId;
    }

    public void setDurationName(String durationName) {
        this.durationName = durationName;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getDurationId() {
        return durationId;
    }

    public String getDurationName() {
        return durationName;
    }

    public Integer getDurationDays() {
        return durationDays;
    }
}

