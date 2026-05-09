package com.wifi.access.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {

    private Integer port;
    private String listenAddress;
    private String captureHost;
}
