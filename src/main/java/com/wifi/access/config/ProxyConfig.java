package com.wifi.access.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "proxy")
@Data
public class ProxyConfig {

    private Integer port;
    private String listenAddress;
    private String captureHost;
}

