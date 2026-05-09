package com.wifi.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessCheckResult {

    private Boolean allowed;
    private String macAddress;
    private Long vkUserId;
    private Boolean isSubscribed;
    private String reason;
    private Long userId;

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Long getVkUserId() {
        return vkUserId;
    }

    public void setVkUserId(Long vkUserId) {
        this.vkUserId = vkUserId;
    }

    public Boolean getIsSubscribed() {
        return isSubscribed;
    }

    public void setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

