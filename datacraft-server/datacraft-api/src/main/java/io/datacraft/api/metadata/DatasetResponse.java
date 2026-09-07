package io.datacraft.api.metadata;

import java.time.Instant;

public record DatasetResponse(Long id, Long datasourceId, String catalogName, String schemaName, String tableName,
                              String tableRemark, Long estimatedRowCount, Instant collectedAt) {
}
