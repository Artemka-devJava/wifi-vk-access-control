package com.wifi.access.router;

import com.wifi.access.config.RouterConfig;
import com.wifi.access.entity.User;
import com.wifi.access.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DeviceManager {

    private static final Logger log = LoggerFactory.getLogger(DeviceManager.class);
    private final TrafficInterceptor trafficInterceptor;
    private final RouterConnectionManager routerConnectionManager;
    private final UserRepository userRepository;
    private final RouterConfig routerConfig;

    private static final Pattern MAC_PATTERN = Pattern.compile(
            "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})"
    );

    /**
     * Получает список всех подключенных устройств
     */
    public void scanConnectedDevices() {
        try {
            String clients = trafficInterceptor.getConnectedClients();
            String[] lines = clients.split("\n");

            for (String line : lines) {
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    String macAddress = parts[1].toUpperCase();

                    if (isValidMacAddress(macAddress)) {
                        registerOrUpdateDevice(macAddress);
                    }
                }
            }
            log.info("Device scan completed");
        } catch (Exception e) {
            log.error("Error scanning devices", e);
        }
    }

    /**
     * Регистрирует новое устройство или обновляет время последнего подключения
     */
    public User registerOrUpdateDevice(String macAddress) {
        long currentTime = Instant.now().getEpochSecond();

        Optional<User> existingUser = userRepository.findByMacAddress(macAddress);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastSeenTime(currentTime);
            userRepository.save(user);
            log.debug("Updated device {}", macAddress);
            return user;
        } else {
            User newUser = User.builder()
                    .macAddress(macAddress)
                    .firstSeenTime(currentTime)
                    .lastSeenTime(currentTime)
                    .isSubscribed(false)
                    .deviceName("Unknown")
                    .build();
            userRepository.save(newUser);
            log.info("Registered new device {}", macAddress);
            return newUser;
        }
    }

    /**
     * Получает пользователя по MAC адресу
     */
    public Optional<User> getUserByMacAddress(String macAddress) {
        return userRepository.findByMacAddress(macAddress);
    }

    /**
     * Обновляет VK User ID для устройства
     */
    public void updateVkUserId(String macAddress, Long vkUserId) {
        Optional<User> userOpt = userRepository.findByMacAddress(macAddress);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVkUserId(vkUserId);
            userRepository.save(user);
            log.info("Updated VK User ID for device {}: {}", macAddress, vkUserId);
        }
    }

    /**
     * Обновляет статус подписки для пользователя
     */
    public void updateSubscriptionStatus(Long userId, Boolean isSubscribed) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsSubscribed(isSubscribed);
            user.setLastCheckTime(Instant.now().getEpochSecond());
            userRepository.save(user);
            log.info("Updated subscription status for user {}: {}", user.getMacAddress(), isSubscribed);
        }
    }

    /**
     * Проверяет валидность MAC адреса
     */
    public boolean isValidMacAddress(String macAddress) {
        Matcher matcher = MAC_PATTERN.matcher(macAddress);
        return matcher.matches();
    }

    /**
     * Получает IP адрес устройства по MAC адресу (через arp)
     */
    public String getDeviceIpAddress(String macAddress) {
        try {
            String arpOutput = trafficInterceptor.getConnectedDevices();
            String[] lines = arpOutput.split("\n");

            for (String line : lines) {
                if (line.contains(macAddress)) {
                    Pattern ipPattern = Pattern.compile(
                            "\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b"
                    );
                    Matcher matcher = ipPattern.matcher(line);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting device IP address", e);
        }
        return null;
    }
}

