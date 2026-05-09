package com.wifi.access.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vk-api")
public class VkApiConfig {
    private String accessToken;
    private Long groupId;
    private String apiVersion;
    private String baseUrl;
}

