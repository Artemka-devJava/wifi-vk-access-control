package com.wifi.access.repository;

import com.wifi.access.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    List<AccessLog> findByMacAddress(String macAddress);

    List<AccessLog> findByUserId(Long userId);

    List<AccessLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<AccessLog> findByActionAndTimestampAfter(String action, LocalDateTime time);
}

