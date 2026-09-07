package io.datacraft.execution.domain;

import java.util.Map;

public record DataReference(DataReferenceType type, String uri, Map<String, Object> metadata) {
    public DataReference {
        if (type == null) {
            throw new IllegalArgumentException("data reference type must not be null");
        }
        if (uri == null || uri.isBlank()) {
            throw new IllegalArgumentException("data reference uri must not be blank");
        }
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
    }
}
