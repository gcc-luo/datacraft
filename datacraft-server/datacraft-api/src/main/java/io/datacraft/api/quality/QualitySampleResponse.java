package io.datacraft.api.quality;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;

public record QualitySampleResponse(Long id, Integer sampleIndex, Map<String, Object> data) {
    public QualitySampleResponse {
        data = Collections.unmodifiableMap(new LinkedHashMap<>(data == null ? Map.of() : data));
    }
}
