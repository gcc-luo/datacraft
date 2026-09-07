package io.datacraft.pipeline.domain;

import io.datacraft.api.pipeline.ExecutionStrategy;
import io.datacraft.api.pipeline.PipelineStatus;

import java.time.Instant;
import java.util.List;

public record Pipeline(Long id, String name, String description, PipelineStatus status, int version,
                       ExecutionStrategy executionStrategy, String graphJson, Long createdBy,
                       Instant createdAt, Instant updatedAt, List<PipelineNode> nodes,
                       List<PipelineEdge> edges) {
}
