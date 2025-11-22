package com.example.cube.model;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_channel", schema = "public")
public class NotificationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Integer channelId;

    @Column(name = "channel_name", nullable = false, unique = true)
    private String channelName; // e.g., "email", "in_app", "push"

    // Getters and Setters
    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}

