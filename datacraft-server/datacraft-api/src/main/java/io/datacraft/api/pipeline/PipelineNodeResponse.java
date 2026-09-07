package io.datacraft.api.pipeline;

public record PipelineNodeResponse(Long id, String nodeKey, String nodeType, String nodeName,
                                   Double x, Double y, String configJson, String preferredEngine) {
}
