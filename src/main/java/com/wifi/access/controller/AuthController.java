package com.wifi.access.controller;

import com.wifi.access.dto.AccessCheckResult;
import com.wifi.access.entity.User;
import com.wifi.access.service.AccessController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST контроллер для управления доступом и аутентификацией
 */
@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AccessController accessController;

    /**
     * Проверяет доступ пользователя в интернет
     * GET /api/auth/check-access?macAddress=XX:XX:XX:XX:XX:XX&ipAddress=192.168.1.100&url=http://example.com
     */
    @GetMapping("/check-access")
    public ResponseEntity<AccessCheckResult> checkAccess(
            @RequestParam String macAddress,
            @RequestParam String ipAddress,
            @RequestParam(defaultValue = "") String url) {
        try {
            AccessCheckResult result = accessController.checkAccess(macAddress, ipAddress, url);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking access", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Связывает MAC адрес с VK User ID
     * POST /api/auth/link-device?macAddress=XX:XX:XX:XX:XX:XX&vkUserId=123456789
     */
    @PostMapping("/link-device")
    public ResponseEntity<String> linkDevice(
            @RequestParam String macAddress,
            @RequestParam Long vkUserId) {
        try {
            accessController.associateMacWithVkUser(macAddress, vkUserId);
            return ResponseEntity.ok("Device linked successfully");
        } catch (Exception e) {
            log.error("Error linking device", e);
            return ResponseEntity.status(500).body("Error linking device: " + e.getMessage());
        }
    }

    /**
     * Получает информацию о пользователе по MAC адресу
     * GET /api/auth/user?macAddress=XX:XX:XX:XX:XX:XX
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserByMacAddress(@RequestParam String macAddress) {
        try {
            Optional<User> user = accessController.getUserByMacAddress(macAddress);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting user", e);
            return ResponseEntity.status(500).body("Error getting user: " + e.getMessage());
        }
    }

    /**
     * Получает информацию о пользователе по VK ID
     * GET /api/auth/user-by-vk?vkUserId=123456789
     */
    @GetMapping("/user-by-vk")
    public ResponseEntity<?> getUserByVkId(@RequestParam Long vkUserId) {
        try {
            Optional<User> user = accessController.getUserByVkId(vkUserId);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting user by VK ID", e);
            return ResponseEntity.status(500).body("Error getting user: " + e.getMessage());
        }
    }
}

