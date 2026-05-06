package com.wifi.access.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String macAddress;

    @Column(name = "vk_user_id")
    private Long vkUserId;

    @Column(name = "is_subscribed")
    private Boolean isSubscribed;

    @Column(name = "last_check_time")
    private Long lastCheckTime;

    @Column(name = "first_seen_time", nullable = false)
    private Long firstSeenTime;

    @Column(name = "last_seen_time", nullable = false)
    private Long lastSeenTime;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

