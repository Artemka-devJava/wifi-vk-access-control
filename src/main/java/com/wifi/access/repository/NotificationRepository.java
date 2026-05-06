package com.wifi.access.repository;

import com.wifi.access.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByVkUserId(Long vkUserId);

    List<Notification> findByUserId(Long userId);

    List<Notification> findByIsReadFalse();

    List<Notification> findByMessageType(String messageType);
}

