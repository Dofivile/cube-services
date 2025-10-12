package com.example.cube.model;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "\"Cubes\"", schema = "public")
public class Cube {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cube_id", columnDefinition = "uuid")
    private UUID cubeId;

    @Column(nullable = false)
    private String name;

    private String description;

    // Foreign key linking to UserDetails.userid (the cube creator)
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID user_id;

    @Column(name = "amount_per_cycle", nullable = false)
    private BigDecimal amountPerCycle;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(nullable = false)
    private Integer numberofmembers;

    @ManyToOne
    @JoinColumn(name = "duration_id", referencedColumnName = "duration_id")
    private DurationOption duration;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "next_payout_date")
    private Instant nextPayoutDate;

    public void setCubeId(UUID cubeId) {
        this.cubeId = cubeId;
    }

    public void setDuration(DurationOption duration) {
        this.duration = duration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public void setAmountPerCycle(BigDecimal amountPerCycle) {
        this.amountPerCycle = amountPerCycle;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setNumberofmembers(Integer numberofmembers) {
        this.numberofmembers = numberofmembers;
    }


    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public void setNextPayoutDate(Instant nextPayoutDate) {
        this.nextPayoutDate = nextPayoutDate;
    }

    public UUID getCubeId() {
        return cubeId;
    }

    public DurationOption getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public BigDecimal getAmountPerCycle() {
        return amountPerCycle;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getNumberofmembers() {
        return numberofmembers;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public Instant getNextPayoutDate() {
        return nextPayoutDate;
    }
}
