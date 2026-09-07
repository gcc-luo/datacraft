package com.datacraft.quality.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public record QualitySample(Long id, Long qualityResultId, int sampleIndex, Map<String, Object> data) {
    public QualitySample {
        data = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(data == null ? Map.of() : data));
    }
}
