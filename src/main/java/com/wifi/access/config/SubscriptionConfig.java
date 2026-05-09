package com.wifi.access.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionConfig {

    private Integer cacheTtlMinutes;
    private Boolean checkOnNewDevice;
    private String notificationMessage;
}
