package com.wifi.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WifiAccessControlApp {

    public static void main(String[] args) {
        SpringApplication.run(WifiAccessControlApp.class, args);
    }
}

