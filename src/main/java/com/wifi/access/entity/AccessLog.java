package com.wifi.access.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mac_address", nullable = false)
    private String macAddress;

    @Column(name = "action", nullable = false)
    private String action; // 'allowed', 'blocked', 'notified'

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "requested_url")
    private String requestedUrl;

    @Column(name = "reason")
    private String reason;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}

