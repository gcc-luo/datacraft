package io.datacraft.pipeline.domain;

import io.datacraft.api.pipeline.NodeCategory;

import java.util.List;
import java.util.Map;

public record NodeMetadata(String type, String name, NodeCategory category, String icon,
                           List<String> supportedEngines, String defaultEngine,
                           Map<String, Object> configSchema) {
    public NodeMetadata {
        supportedEngines = List.copyOf(supportedEngines == null ? List.of() : supportedEngines);
        configSchema = Map.copyOf(configSchema == null ? Map.of() : configSchema);
    }
}
