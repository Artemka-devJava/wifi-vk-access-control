package com.wifi.access.vk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wifi.access.config.SubscriptionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class VkSubscriptionChecker {

    private final VkApiClient vkApiClient;
    private final SubscriptionConfig subscriptionConfig;
    private Cache<Long, Boolean> subscriptionCache;

    @PostConstruct
    public void init() {
        subscriptionCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(subscriptionConfig.getCacheTtlMinutes(), TimeUnit.MINUTES)
                .build();
    }

    /**
     * Проверяет подписку пользователя с кешированием
     */
    public boolean isSubscribed(Long vkUserId) {
        Boolean cached = subscriptionCache.getIfPresent(vkUserId);
        if (cached != null) {
            log.debug("Using cached subscription status for user {}: {}", vkUserId, cached);
            return cached;
        }

        boolean isSubscribed = vkApiClient.isMember(vkUserId);
        subscriptionCache.put(vkUserId, isSubscribed);
        log.info("Checked subscription for user {}: {}", vkUserId, isSubscribed);
        return isSubscribed;
    }

    /**
     * Инвалидирует кеш подписки для пользователя
     */
    public void invalidateCache(Long vkUserId) {
        subscriptionCache.invalidate(vkUserId);
        log.debug("Invalidated cache for user {}", vkUserId);
    }

    /**
     * Очищает весь кеш
     */
    public void clearCache() {
        subscriptionCache.invalidateAll();
        log.info("Subscription cache cleared");
    }
}

