package com.wifi.access.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "router")
public class RouterConfig {

    private String host;
    private Integer sshPort;
    private String username;
    private String password;
    private String wifiInterface;

    // Geters and Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWifiInterface() {
        return wifiInterface;
    }

    public void setWifiInterface(String wifiInterface) {
        this.wifiInterface = wifiInterface;
    }
}

