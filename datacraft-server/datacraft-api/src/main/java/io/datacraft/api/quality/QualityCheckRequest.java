package io.datacraft.api.quality;

import java.util.List;

public record QualityCheckRequest(
        Long datasourceId,
        Long executionId,
        Long nodeExecutionId,
        String tableName,
        List<QualityRuleRequest> rules
) {
    public QualityCheckRequest {
        rules = List.copyOf(rules == null ? List.of() : rules);
    }
}
