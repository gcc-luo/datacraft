package com.datacraft.api.pipeline;

import java.time.Instant;

public record PipelineResponse(Long id, String name, String description, PipelineStatus status,
                               int version, ExecutionStrategy executionStrategy,
                               Instant createdAt, Instant updatedAt) {
}
