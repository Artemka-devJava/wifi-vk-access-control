package com.wifi.access.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vk_user_id")
    private Long vkUserId;

    @Column(name = "mac_address", nullable = false)
    private String macAddress;

    @Column(name = "message_type")
    private String messageType; // 'subscription_request', 'welcome', 'reminder'

    @Column(name = "vk_message_id")
    private Long vkMessageId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    private Boolean isRead;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        isRead = false;
    }
}

