package com.datacraft.api.quality;

import java.time.Instant;
import java.util.List;

public record QualityResultResponse(
        Long id,
        Long executionId,
        Long nodeExecutionId,
        String ruleType,
        Long datasourceId,
        String tableName,
        String fieldName,
        long totalRows,
        long errorRows,
        long passRows,
        double passRate,
        String status,
        List<QualitySampleResponse> samples,
        Instant createdAt
) {
    public QualityResultResponse {
        samples = List.copyOf(samples == null ? List.of() : samples);
    }
}
