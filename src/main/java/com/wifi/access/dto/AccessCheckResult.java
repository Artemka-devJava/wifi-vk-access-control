package com.wifi.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
}

