package io.datacraft.pipeline.domain;

public record PipelineEdge(Long id, String sourceNodeKey, String targetNodeKey,
                           String sourcePort, String targetPort, String conditionJson) {
}
