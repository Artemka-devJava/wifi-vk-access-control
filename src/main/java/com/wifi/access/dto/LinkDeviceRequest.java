package com.wifi.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса связывания MAC адреса с VK пользователем
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkDeviceRequest {

    private String macAddress;
    private Long vkUserId;
    private String deviceName;
}

