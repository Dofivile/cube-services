package com.example.cube.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_type", schema = "public")
public class NotificationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "type_key", nullable = false, unique = true)
    private String typeKey; // e.g., "cube_ready_admin", "cube_ready_member"

    @Column(name = "description", columnDefinition = "text")
    private String description;

    // Getters and Setters
    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

