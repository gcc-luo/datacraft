package com.datacraft.quality.domain;

import java.time.Instant;

public record QualityResult(Long id, Long executionId, Long nodeExecutionId, QualityRuleType ruleType,
                            Long datasourceId, String tableName, String fieldName, long totalRows,
                            long errorRows, long passRows, double passRate, String status, Instant createdAt) {
}
