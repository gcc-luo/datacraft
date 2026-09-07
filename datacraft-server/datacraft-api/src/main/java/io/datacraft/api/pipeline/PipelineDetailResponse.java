package io.datacraft.api.pipeline;

import java.time.Instant;
import java.util.List;

public record PipelineDetailResponse(Long id, String name, String description, PipelineStatus status,
                                     int version, ExecutionStrategy executionStrategy,
                                     Instant createdAt, Instant updatedAt,
                                     List<PipelineNodeResponse> nodes, List<PipelineEdgeResponse> edges) {
}
