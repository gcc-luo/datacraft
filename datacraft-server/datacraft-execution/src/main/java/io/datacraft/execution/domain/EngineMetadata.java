package io.datacraft.execution.domain;

import io.datacraft.api.execution.EngineDeploymentMode;

import java.util.List;

public record EngineMetadata(String code, String name, EngineDeploymentMode deploymentMode,
                             boolean enabled, List<String> capabilities) {
    public EngineMetadata {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("engine code must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("engine name must not be blank");
        }
        if (deploymentMode == null) {
            throw new IllegalArgumentException("deployment mode must not be null");
        }
        capabilities = List.copyOf(capabilities == null ? List.of() : capabilities);
    }

    public static EngineMetadata test(String code) {
        return new EngineMetadata(code, code, EngineDeploymentMode.EMBEDDED, true, List.of());
    }
}
