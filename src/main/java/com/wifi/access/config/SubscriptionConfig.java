package com.wifi.access.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionConfig {

    private Integer cacheTtlMinutes;
    private Boolean checkOnNewDevice;
    private String notificationMessage;

    public Integer getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }

    public void setCacheTtlMinutes(Integer cacheTtlMinutes) {
        this.cacheTtlMinutes = cacheTtlMinutes;
    }

    public Boolean getCheckOnNewDevice() {
        return checkOnNewDevice;
    }

    public void setCheckOnNewDevice(Boolean checkOnNewDevice) {
        this.checkOnNewDevice = checkOnNewDevice;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}

