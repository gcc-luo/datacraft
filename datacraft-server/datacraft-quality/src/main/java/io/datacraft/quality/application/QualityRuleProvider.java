package io.datacraft.quality.application;

import io.datacraft.api.quality.QualityRuleRequest;
import io.datacraft.quality.domain.QualityRuleType;

import java.util.List;

public interface QualityRuleProvider {
    QualityRuleType type();

    String predicate(QualityRuleRequest request, SqlDialect dialect);

    default List<Object> parameters(QualityRuleRequest request) {
        return List.of();
    }

    void validate(QualityRuleRequest request);
}
