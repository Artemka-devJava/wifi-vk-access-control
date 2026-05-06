package com.wifi.access.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "router")
@Data
public class RouterConfig {

    private String host;
    private Integer sshPort;
    private String username;
    private String password;
    private String wifiInterface;
}

