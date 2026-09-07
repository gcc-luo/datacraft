package io.datacraft.execution.web;

import io.datacraft.api.execution.EngineHealthResponse;
import io.datacraft.api.execution.EngineResponse;
import io.datacraft.common.web.ApiResponse;
import io.datacraft.execution.application.EngineRegistry;
import io.datacraft.execution.domain.EngineHealth;
import io.datacraft.execution.domain.EngineMetadata;
import io.datacraft.execution.domain.ExecutionEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/engines")
public class EngineController {
    private final EngineRegistry registry;

    public EngineController(EngineRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public ApiResponse<List<EngineResponse>> list() {
        return ApiResponse.success(registry.list().stream().map(this::toResponse).toList());
    }

    private EngineResponse toResponse(ExecutionEngine engine) {
        EngineMetadata metadata = engine.metadata();
        EngineHealth health = engine.healthCheck();
        return new EngineResponse(metadata.code(), metadata.name(), metadata.deploymentMode(), metadata.enabled(),
                metadata.capabilities(), new EngineHealthResponse(
                io.datacraft.api.execution.EngineHealthStatus.valueOf(health.status().name()),
                health.message(), health.checkedAt()));
    }
}
