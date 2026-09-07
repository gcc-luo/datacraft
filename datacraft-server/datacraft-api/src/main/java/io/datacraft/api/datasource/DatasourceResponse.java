package io.datacraft.api.datasource;

import java.time.Instant;

public record DatasourceResponse(
        Long id,
        String name,
        DatasourceType type,
        String host,
        Integer port,
        String databaseName,
        String username,
        String remark,
        DatasourceStatus status,
        Instant lastTestedAt,
        Long lastTestLatencyMs,
        String lastTestMessage,
        Instant createdAt,
        Instant updatedAt
) {
}
