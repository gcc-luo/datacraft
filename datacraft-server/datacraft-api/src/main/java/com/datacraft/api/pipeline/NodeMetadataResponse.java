package com.datacraft.api.pipeline;

import java.util.List;
import java.util.Map;

public record NodeMetadataResponse(String type, String name, NodeCategory category, String icon,
                                   List<String> supportedEngines, String defaultEngine,
                                   Map<String, Object> configSchema) {
}
