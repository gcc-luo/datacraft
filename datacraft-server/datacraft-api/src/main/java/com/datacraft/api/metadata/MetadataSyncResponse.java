package com.datacraft.api.metadata;

import java.time.Instant;

public record MetadataSyncResponse(Long datasourceId, int schemaCount, int datasetCount, int fieldCount,
                                   Instant collectedAt) {
}
