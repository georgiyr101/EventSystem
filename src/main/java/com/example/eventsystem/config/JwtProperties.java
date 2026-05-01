package com.example.eventsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    /**
     * HMAC secret (bytes length should be sufficient for HS256).
     */
    private String secret;

    /**
     * Access token TTL in milliseconds.
     */
    private long expirationMs;
}
