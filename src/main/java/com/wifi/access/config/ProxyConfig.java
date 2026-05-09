package com.wifi.access.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {

    private Integer port;
    private String listenAddress;
    private String captureHost;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public String getCaptureHost() {
        return captureHost;
    }

    public void setCaptureHost(String captureHost) {
        this.captureHost = captureHost;
    }
}

