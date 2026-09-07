package com.datacraft.datasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;

/** Configuration for the key used to encrypt datasource credentials. */
@ConfigurationProperties(prefix = "datacraft.security.datasource")
public record DatasourceEncryptionProperties(String encryptionKey) {

    public DatasourceEncryptionProperties(String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("DATACRAFT_DATASOURCE_ENCRYPTION_KEY must be configured");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encryptionKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("DATACRAFT_DATASOURCE_ENCRYPTION_KEY must be valid Base64", exception);
        }
        if (decoded.length != 32) {
            throw new IllegalStateException("DATACRAFT_DATASOURCE_ENCRYPTION_KEY must decode to 32 bytes");
        }
        this.encryptionKey = encryptionKey;
    }

    public byte[] keyBytes() {
        return Base64.getDecoder().decode(encryptionKey);
    }
}
