package com.wifi.access.service;

import com.wifi.access.router.DeviceManager;
import com.wifi.access.router.RouterConnectionManager;
import com.wifi.access.router.TrafficInterceptor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Сервис инициализации и планирования задач приложения
 */
@Service
@RequiredArgsConstructor
public class ApplicationInitializationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationInitializationService.class);
    private final RouterConnectionManager routerConnectionManager;
    private final TrafficInterceptor trafficInterceptor;
    private final DeviceManager deviceManager;

    /**
     * Инициализирует приложение после его загрузки
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("============================================");
        log.info("WiFi VK Access Control Service Starting...");
        log.info("============================================");

        try {
            // Подключаемся к роутеру
            routerConnectionManager.connect();
            log.info("✓ Connected to router");

            // Устанавливаем правила перехвата трафика
            trafficInterceptor.setupTrafficInterception();
            log.info("✓ Traffic interception rules installed");

            // Сканируем подключенные устройства
            deviceManager.scanConnectedDevices();
            log.info("✓ Connected devices scanned");

            log.info("============================================");
            log.info("WiFi VK Access Control Service Ready!");
            log.info("============================================");
        } catch (Exception e) {
            log.error("Error during application initialization", e);
        }
    }

    /**
     * Периодически сканирует новые устройства (каждые 5 минут)
     * Конвертация: 5 минут = 300000 мс, 1 минута = 60000 мс
     */
    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    public void scanDevicesPeriodically() {
        try {
            log.debug("Periodic device scan started");
            deviceManager.scanConnectedDevices();
            log.debug("Periodic device scan completed");
        } catch (Exception e) {
            log.error("Error during periodic device scan", e);
        }
    }

    /**
     * Очищает кеш подписок (каждый час)
     * Конвертация: 1 час = 3600000 мс, 30 минут = 1800000 мс
     */
    @Scheduled(fixedRate = 3600000, initialDelay = 1800000)
    public void refreshSubscriptionCache() {
        log.debug("Subscription cache refresh scheduled");
        // Кеш будет автоматически инвалидирован через TTL
        // Это просто для логирования
    }
}

