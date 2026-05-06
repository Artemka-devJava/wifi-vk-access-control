package com.wifi.access;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.wifi.access")
public class WifiAccessControlApp {

    public static void main(String[] args) {
        SpringApplication.run(WifiAccessControlApp.class, args);
    }
}

