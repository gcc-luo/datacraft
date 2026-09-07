package io.datacraft.api.datasource;

import java.time.Instant;

public record DatasourceTestResponse(
        boolean success,
        DatasourceStatus status,
        Long latencyMs,
        String message,
        Instant testedAt
) {
}
