package com.datacraft.execution.domain;

import java.time.Instant;

public record EngineHealth(EngineHealthStatus status, String message, Instant checkedAt) {
    public EngineHealth {
        if (status == null) {
            throw new IllegalArgumentException("health status must not be null");
        }
        if (checkedAt == null) {
            throw new IllegalArgumentException("health check time must not be null");
        }
    }
}
