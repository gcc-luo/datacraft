package io.datacraft.api.pipeline;

public record PipelineEdgeRequest(String sourceNodeKey, String targetNodeKey,
                                  String sourcePort, String targetPort, String conditionJson) {
}
