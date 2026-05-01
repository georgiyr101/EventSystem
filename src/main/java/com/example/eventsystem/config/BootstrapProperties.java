package com.example.eventsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {
    private String adminEmail;
    private String adminPassword;
    private String adminFullName;
}
