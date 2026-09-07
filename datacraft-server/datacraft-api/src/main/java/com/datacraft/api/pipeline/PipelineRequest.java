package com.datacraft.api.pipeline;

import java.util.List;

public record PipelineRequest(String name, String description, PipelineStatus status,
                              ExecutionStrategy executionStrategy, List<PipelineNodeRequest> nodes,
                              List<PipelineEdgeRequest> edges) {
}
