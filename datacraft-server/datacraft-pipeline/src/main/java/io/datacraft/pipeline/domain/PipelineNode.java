package io.datacraft.pipeline.domain;

public record PipelineNode(Long id, String nodeKey, String nodeType, String nodeName,
                           Double x, Double y, String configJson, String preferredEngine) {
}
