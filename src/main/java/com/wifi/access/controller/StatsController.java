package com.wifi.access.controller;

import com.wifi.access.entity.AccessLog;
import com.wifi.access.entity.User;
import com.wifi.access.repository.AccessLogRepository;
import com.wifi.access.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для получения статистики и логов
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private static final Logger log = LoggerFactory.getLogger(StatsController.class);

    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;

    /**
     * Получает общую статистику
     * GET /api/stats/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        try {
            Map<String, Object> stats = new HashMap<>();

            List<User> allUsers = userRepository.findAll();
            List<User> subscribedUsers = userRepository.findByIsSubscribedTrue();
            List<User> unsubscribedUsers = userRepository.findByIsSubscribedFalse();

            stats.put("total_devices", allUsers.size());
            stats.put("subscribed_devices", subscribedUsers.size());
            stats.put("unsubscribed_devices", unsubscribedUsers.size());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting summary", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Получает список всех устройств
     * GET /api/stats/devices?page=0&size=20
     */
    @GetMapping("/devices")
    public ResponseEntity<?> getDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<User> devices = userRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            log.error("Error getting devices", e);
            return ResponseEntity.status(500).body("Error getting devices: " + e.getMessage());
        }
    }

    /**
     * Получает логи доступа для устройства
     * GET /api/stats/access-logs?macAddress=XX:XX:XX:XX:XX:XX
     */
    @GetMapping("/access-logs")
    public ResponseEntity<?> getAccessLogs(@RequestParam String macAddress) {
        try {
            List<AccessLog> logs = accessLogRepository.findByMacAddress(macAddress);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error getting access logs", e);
            return ResponseEntity.status(500).body("Error getting logs: " + e.getMessage());
        }
    }

    /**
     * Получает статистику по пользователю
     * GET /api/stats/user?userId=1
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserStats(@RequestParam Long userId) {
        try {
            var user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> userStats = new HashMap<>();
            User u = user.get();
            userStats.put("user", u);
            userStats.put("access_logs", accessLogRepository.findByUserId(userId));

            return ResponseEntity.ok(userStats);
        } catch (Exception e) {
            log.error("Error getting user stats", e);
            return ResponseEntity.status(500).body("Error getting user stats: " + e.getMessage());
        }
    }
}

