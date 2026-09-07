package com.datacraft.execution.web;

import com.datacraft.api.execution.EngineHealthResponse;
import com.datacraft.api.execution.EngineResponse;
import com.datacraft.common.web.ApiResponse;
import com.datacraft.execution.application.EngineNotFoundException;
import com.datacraft.execution.application.EngineRegistry;
import com.datacraft.execution.domain.EngineHealth;
import com.datacraft.execution.domain.EngineMetadata;
import com.datacraft.execution.domain.ExecutionEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/{code}")
    public ApiResponse<EngineResponse> get(@PathVariable String code) {
        return ApiResponse.success(toResponse(registry.get(code)));
    }

    @PostMapping("/{code}/test")
    public ApiResponse<EngineHealthResponse> test(@PathVariable String code) {
        ExecutionEngine engine = registry.get(code);
        EngineHealth health = engine.healthCheck();
        return ApiResponse.success(toHealthResponse(health));
    }

    @PutMapping("/{code}")
    public ApiResponse<EngineResponse> update(@PathVariable String code) {
        return ApiResponse.success(toResponse(registry.get(code)));
    }

    private EngineResponse toResponse(ExecutionEngine engine) {
        EngineMetadata metadata = engine.metadata();
        EngineHealth health = engine.healthCheck();
        return new EngineResponse(metadata.code(), metadata.name(), metadata.deploymentMode(), metadata.enabled(),
                metadata.capabilities(), toHealthResponse(health));
    }

    private EngineHealthResponse toHealthResponse(EngineHealth health) {
        return new EngineHealthResponse(
                com.datacraft.api.execution.EngineHealthStatus.valueOf(health.status().name()),
                health.message(), health.checkedAt());
    }

    @ExceptionHandler(EngineNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(EngineNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure("ENGINE_NOT_FOUND", exception.getMessage()));
    }
}
