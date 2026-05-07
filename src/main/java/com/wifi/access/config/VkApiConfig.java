package com.wifi.access.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vk-api")
@Data
public class VkApiConfig {

    private String accessToken;
    private Long groupId;
    private String apiVersion;
    private String baseUrl;
}

