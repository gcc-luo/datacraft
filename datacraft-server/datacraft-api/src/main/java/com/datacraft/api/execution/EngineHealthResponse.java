package com.datacraft.api.execution;

import java.time.Instant;

public record EngineHealthResponse(EngineHealthStatus status, String message, Instant checkedAt) {
}
