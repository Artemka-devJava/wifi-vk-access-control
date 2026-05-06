package com.wifi.access.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "subscription")
@Data
public class SubscriptionConfig {

    private Integer cacheTtlMinutes;
    private Boolean checkOnNewDevice;
    private String notificationMessage;
}

