package io.datacraft.pipeline.application;

import io.datacraft.api.pipeline.NodeCategory;
import io.datacraft.pipeline.domain.NodeMetadata;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NodeRegistry {
    private final Map<String, NodeMetadata> metadataByType;

    public NodeRegistry() {
        List<NodeMetadata> builtIns = List.of(
                new NodeMetadata("DATABASE_SOURCE", "Database Source", NodeCategory.SOURCE, "database",
                        List.of("NATIVE", "DATAX"), "NATIVE", schema("datasourceId", "tableName")),
                new NodeMetadata("FILTER", "Filter", NodeCategory.TRANSFORM, "filter",
                        List.of("NATIVE"), "NATIVE", schema("expression")),
                new NodeMetadata("DATABASE_SINK", "Database Sink", NodeCategory.SINK, "database",
                        List.of("NATIVE", "DATAX"), "NATIVE", schema("datasourceId", "tableName", "writeMode"))
        );
        metadataByType = builtIns.stream().collect(Collectors.toUnmodifiableMap(NodeMetadata::type, value -> value));
    }

    public List<NodeMetadata> list() {
        return metadataByType.values().stream()
                .sorted(Comparator.comparing(NodeMetadata::type))
                .toList();
    }

    public Set<String> types() {
        return metadataByType.keySet();
    }

    public NodeMetadata get(String type) {
        NodeMetadata metadata = metadataByType.get(type);
        if (metadata == null) {
            throw new NodeTypeNotFoundException(type);
        }
        return metadata;
    }

    private static Map<String, Object> schema(String... fields) {
        return Map.of("fields", List.of(fields));
    }
}
