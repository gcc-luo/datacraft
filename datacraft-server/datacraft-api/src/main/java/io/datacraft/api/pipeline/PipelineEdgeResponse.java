package io.datacraft.api.pipeline;

public record PipelineEdgeResponse(Long id, String sourceNodeKey, String targetNodeKey,
                                   String sourcePort, String targetPort, String conditionJson) {
}
