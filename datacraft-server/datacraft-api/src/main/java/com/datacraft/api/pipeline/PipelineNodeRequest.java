package com.datacraft.api.pipeline;

public record PipelineNodeRequest(String nodeKey, String nodeType, String nodeName,
                                  Double x, Double y, String configJson, String preferredEngine) {
}
