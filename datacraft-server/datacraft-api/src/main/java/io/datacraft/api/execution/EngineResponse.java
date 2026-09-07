package io.datacraft.api.execution;

import java.util.List;

public record EngineResponse(String code, String name, EngineDeploymentMode deploymentMode,
                             boolean enabled, List<String> capabilities, EngineHealthResponse health) {
    public EngineResponse {
        capabilities = List.copyOf(capabilities == null ? List.of() : capabilities);
    }
}
