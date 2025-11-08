package com.example.cube.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cubes", schema = "public")
public class Cube {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cube_id", columnDefinition = "uuid")
    private UUID cubeId;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID user_id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "amount_per_cycle", nullable = false)
    private BigDecimal amountPerCycle;

    @Column(nullable = false)
    private String currency = "USD";

    @Column(nullable = false)
    private Integer numberofmembers;

    @ManyToOne
    @JoinColumn(name = "duration_id", referencedColumnName = "duration_id")
    private DurationOption duration;

    @Column(name = "next_payout_date")
    private Instant nextPayoutDate;

    @Column(name = "current_cycle")
    private Integer currentCycle;

    @Column(name = "total_amount_collected")
    private BigDecimal totalAmountCollected = BigDecimal.ZERO;


    @Column(name = "status_id", nullable = false)
    private Integer statusId = 1;  // Default: draft

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "rotation_id")
    private Integer rotationId;

    @ManyToOne
    @JoinColumn(name = "goal_type_id", referencedColumnName = "goal_type_id")
    private GoalType goalType;

    @Column(name = "goal_type_id")
    private Integer goalTypeId;

    @Column(name = "invitation_code", unique = true, length = 6)
    private String invitationCode;

    @Column(name = "start_date")
    private Instant startDate;

    @Column(name = "end_date")
    private Instant endDate;

    public UUID getCubeId() { return cubeId;}

    public void setCubeId(UUID cubeId) {this.cubeId = cubeId;}

    public String getName() { return name;}

    public void setName(String name) {this.name = name;}

    public String getDescription() {return description; }

    public void setDescription(String description) { this.description = description;}

    public UUID getUser_id() { return user_id; }

    public void setUser_id(UUID user_id) { this.user_id = user_id; }

    public BigDecimal getAmountPerCycle() { return amountPerCycle; }

    public void setAmountPerCycle(BigDecimal amountPerCycle) { this.amountPerCycle = amountPerCycle; }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

    public Integer getNumberofmembers() { return numberofmembers;}

    public void setNumberofmembers(Integer numberofmembers) { this.numberofmembers = numberofmembers;}

    public DurationOption getDuration() { return duration;}

    public void setDuration(DurationOption duration) {this.duration = duration;}

    public Instant getStartDate() { return startDate;}

    public void setStartDate(Instant startDate) {this.startDate = startDate;}

    public Instant getEndDate() {return endDate;}

    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public Instant getNextPayoutDate() { return nextPayoutDate; }

    public void setNextPayoutDate(Instant nextPayoutDate) { this.nextPayoutDate = nextPayoutDate; }

    public Integer getCurrentCycle() { return currentCycle;}

    public void setCurrentCycle(Integer currentCycle) { this.currentCycle = currentCycle; }

    public BigDecimal getTotalAmountCollected() { return totalAmountCollected; }

    public void setTotalAmountCollected(BigDecimal totalAmountCollected) { this.totalAmountCollected = totalAmountCollected;}

    // total_to_be_collected removed from schema; compute on demand where needed

    public Integer getStatusId() { return statusId; }

    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Integer getRotationId() { return rotationId; }

    public void setRotationId(Integer rotationId) { this.rotationId = rotationId; }

    public GoalType getGoalType() { return goalType; }

    public void setGoalType(GoalType goalType) { this.goalType = goalType; }

    public String getInvitationCode() { return invitationCode; }

    public void setInvitationCode(String invitationCode) { this.invitationCode = invitationCode; }
}
