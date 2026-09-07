package com.datacraft.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datacraft.security.jwt")
public record JwtProperties(String secret, long expirationSeconds) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("datacraft.security.jwt.secret must be configured");
        }
        if (expirationSeconds < 0) {
            throw new IllegalArgumentException("datacraft.security.jwt.expiration-seconds must not be negative");
        }
    }
}
