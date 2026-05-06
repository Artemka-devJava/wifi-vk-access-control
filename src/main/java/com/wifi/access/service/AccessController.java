package com.wifi.access.service;

import com.wifi.access.config.SubscriptionConfig;
import com.wifi.access.dto.AccessCheckResult;
import com.wifi.access.entity.AccessLog;
import com.wifi.access.entity.User;
import com.wifi.access.repository.AccessLogRepository;
import com.wifi.access.repository.UserRepository;
import com.wifi.access.router.DeviceManager;
import com.wifi.access.vk.VkNotificationSender;
import com.wifi.access.vk.VkSubscriptionChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessController {

    private final DeviceManager deviceManager;
    private final VkSubscriptionChecker subscriptionChecker;
    private final VkNotificationSender notificationSender;
    private final UserRepository userRepository;
    private final AccessLogRepository accessLogRepository;
    private final SubscriptionConfig subscriptionConfig;

    /**
     * Проверяет доступ пользователя в интернет по MAC адресу
     */
    public AccessCheckResult checkAccess(String macAddress, String ipAddress, String requestedUrl) {
        log.info("Checking access for MAC: {}, IP: {}, URL: {}", macAddress, ipAddress, requestedUrl);

        // Регистрируем/обновляем устройство
        User user = deviceManager.registerOrUpdateDevice(macAddress);

        // Если VK User ID не установлен, доступ только к captive portal
        if (user.getVkUserId() == null) {
            log.warn("User {} has no VK ID, redirecting to captive portal", macAddress);
            logAccess(user, "blocked", ipAddress, requestedUrl, "No VK ID associated");
            return AccessCheckResult.builder()
                    .allowed(false)
                    .macAddress(macAddress)
                    .userId(user.getId())
                    .reason("no_vk_id")
                    .build();
        }

        // Проверяем подписку
        boolean isSubscribed = subscriptionChecker.isSubscribed(user.getVkUserId());
        user.setIsSubscribed(isSubscribed);
        user.setLastCheckTime(Instant.now().getEpochSecond());
        userRepository.save(user);

        if (isSubscribed) {
            // Пользователь подписан - разрешаем доступ
            log.info("User {} (MAC: {}) is subscribed, allowing access",
                    user.getVkUserId(), macAddress);
            logAccess(user, "allowed", ipAddress, requestedUrl, "Subscribed to group");

            return AccessCheckResult.builder()
                    .allowed(true)
                    .macAddress(macAddress)
                    .vkUserId(user.getVkUserId())
                    .isSubscribed(true)
                    .userId(user.getId())
                    .reason("subscribed")
                    .build();
        } else {
            // Пользователь не подписан - отправляем уведомление и блокируем
            log.warn("User {} (MAC: {}) is not subscribed, blocking access",
                    user.getVkUserId(), macAddress);
            logAccess(user, "blocked", ipAddress, requestedUrl, "Not subscribed");

            sendNotificationIfNeeded(user);

            return AccessCheckResult.builder()
                    .allowed(false)
                    .macAddress(macAddress)
                    .vkUserId(user.getVkUserId())
                    .isSubscribed(false)
                    .userId(user.getId())
                    .reason("not_subscribed")
                    .build();
        }
    }

    /**
     * Связывает MAC адрес с VK User ID
     */
    public void associateMacWithVkUser(String macAddress, Long vkUserId) {
        User user = deviceManager.registerOrUpdateDevice(macAddress);
        user.setVkUserId(vkUserId);
        userRepository.save(user);

        // Проверяем подписку после привязки
        try {
            boolean isSubscribed = subscriptionChecker.isSubscribed(vkUserId);
            user.setIsSubscribed(isSubscribed);
            userRepository.save(user);

            if (isSubscribed) {
                log.info("User {} is subscribed", vkUserId);
            } else {
                // Отправляем уведомление о необходимости подписки
                sendNotificationIfNeeded(user);
            }
        } catch (Exception e) {
            log.error("Error checking subscription for user {}", vkUserId, e);
        }

        log.info("Associated MAC {} with VK user {}", macAddress, vkUserId);
    }

    /**
     * Отправляет уведомление пользователю о необходимости подписки
     */
    private void sendNotificationIfNeeded(User user) {
        try {
            notificationSender.sendSubscriptionRequestNotification(
                    user,
                    subscriptionConfig.getNotificationMessage()
            );
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }

    /**
     * Логирует попытку доступа
     */
    private void logAccess(User user, String action, String ipAddress,
                          String requestedUrl, String reason) {
        try {
            AccessLog log = AccessLog.builder()
                    .userId(user.getId())
                    .macAddress(user.getMacAddress())
                    .action(action)
                    .ipAddress(ipAddress)
                    .requestedUrl(requestedUrl)
                    .reason(reason)
                    .build();
            accessLogRepository.save(log);
        } catch (Exception e) {
            log.error("Error logging access", e);
        }
    }

    /**
     * Получает информацию о пользователе по MAC адресу
     */
    public Optional<User> getUserByMacAddress(String macAddress) {
        return userRepository.findByMacAddress(macAddress);
    }

    /**
     * Получает информацию о пользователе по VK ID
     */
    public Optional<User> getUserByVkId(Long vkUserId) {
        return userRepository.findByVkUserId(vkUserId);
    }
}

