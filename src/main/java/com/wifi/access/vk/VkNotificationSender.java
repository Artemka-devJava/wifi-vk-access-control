package com.wifi.access.vk;

import com.wifi.access.entity.Notification;
import com.wifi.access.entity.User;
import com.wifi.access.repository.NotificationRepository;
import com.wifi.access.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VkNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(VkNotificationSender.class);
    private final VkApiClient vkApiClient;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Отправляет уведомление о необходимости подписки на группу
     */
    public void sendSubscriptionRequestNotification(User user, String message) {
        try {
            if (user.getVkUserId() == null) {
                log.warn("User {} has no VK ID, cannot send notification", user.getMacAddress());
                return;
            }

            Long messageId = vkApiClient.sendMessage(user.getVkUserId(), message);

            Notification notification = Notification.builder()
                    .userId(user.getId())
                    .vkUserId(user.getVkUserId())
                    .macAddress(user.getMacAddress())
                    .messageType("subscription_request")
                    .vkMessageId(messageId)
                    .build();

            notificationRepository.save(notification);
            log.info("Subscription request notification sent to user {} (VK ID: {})",
                    user.getMacAddress(), user.getVkUserId());
        } catch (Exception e) {
            log.error("Error sending subscription request notification to user {}",
                    user.getMacAddress(), e);
        }
    }

    /**
     * Отправляет приветственное сообщение подписчику
     */
    public void sendWelcomeNotification(User user, String message) {
        try {
            if (user.getVkUserId() == null) {
                log.warn("User {} has no VK ID, cannot send notification", user.getMacAddress());
                return;
            }

            Long messageId = vkApiClient.sendMessage(user.getVkUserId(), message);

            Notification notification = Notification.builder()
                    .userId(user.getId())
                    .vkUserId(user.getVkUserId())
                    .macAddress(user.getMacAddress())
                    .messageType("welcome")
                    .vkMessageId(messageId)
                    .build();

            notificationRepository.save(notification);
            log.info("Welcome notification sent to user {} (VK ID: {})",
                    user.getMacAddress(), user.getVkUserId());
        } catch (Exception e) {
            log.error("Error sending welcome notification to user {}",
                    user.getMacAddress(), e);
        }
    }

    /**
     * Отправляет напоминание о подписке
     */
    public void sendReminderNotification(User user, String message) {
        try {
            if (user.getVkUserId() == null) {
                log.warn("User {} has no VK ID, cannot send notification", user.getMacAddress());
                return;
            }

            Long messageId = vkApiClient.sendMessage(user.getVkUserId(), message);

            Notification notification = Notification.builder()
                    .userId(user.getId())
                    .vkUserId(user.getVkUserId())
                    .macAddress(user.getMacAddress())
                    .messageType("reminder")
                    .vkMessageId(messageId)
                    .build();

            notificationRepository.save(notification);
            log.info("Reminder notification sent to user {} (VK ID: {})",
                    user.getMacAddress(), user.getVkUserId());
        } catch (Exception e) {
            log.error("Error sending reminder notification to user {}",
                    user.getMacAddress(), e);
        }
    }
}

