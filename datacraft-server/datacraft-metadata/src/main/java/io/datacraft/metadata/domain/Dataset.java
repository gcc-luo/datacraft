package io.datacraft.metadata.domain;

import java.time.Instant;
import java.util.List;

public record Dataset(
        Long id,
        Long datasourceId,
        String catalogName,
        String schemaName,
        String tableName,
        String tableRemark,
        Long estimatedRowCount,
        Instant collectedAt,
        List<DatasetField> fields
) {
    public Dataset(Long datasourceId, String catalogName, String schemaName, String tableName,
                   String tableRemark, Long estimatedRowCount, Instant collectedAt, List<DatasetField> fields) {
        this(null, datasourceId, catalogName, schemaName, tableName, tableRemark, estimatedRowCount, collectedAt, fields);
    }

    public Dataset {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }
}
