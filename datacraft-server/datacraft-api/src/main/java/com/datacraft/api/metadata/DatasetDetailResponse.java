package com.datacraft.api.metadata;

import java.time.Instant;
import java.util.List;

public record DatasetDetailResponse(Long id, Long datasourceId, String catalogName, String schemaName,
                                    String tableName, String tableRemark, Long estimatedRowCount,
                                    Instant collectedAt, List<DatasetFieldResponse> fields) {
}
