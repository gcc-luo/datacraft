package io.datacraft.api.quality;

import java.util.List;

public record QualityRuleRequest(
        String type,
        String field,
        Double min,
        Double max,
        String regex,
        Integer minLength,
        Integer maxLength,
        List<String> values,
        String condition
) {
    public QualityRuleRequest {
        values = List.copyOf(values == null ? List.of() : values);
    }
}
