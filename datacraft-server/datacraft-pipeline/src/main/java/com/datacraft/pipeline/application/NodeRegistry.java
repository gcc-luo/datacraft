package com.datacraft.pipeline.application;

import com.datacraft.api.pipeline.NodeCategory;
import com.datacraft.pipeline.domain.NodeMetadata;
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
                new NodeMetadata("NULL_CHECK", "Null Check", NodeCategory.QUALITY, "quality-null",
                        List.of("NATIVE"), "NATIVE", schema("field")),
                new NodeMetadata("UNIQUE_CHECK", "Unique Check", NodeCategory.QUALITY, "quality-unique",
                        List.of("NATIVE"), "NATIVE", schema("field")),
                new NodeMetadata("RANGE_CHECK", "Range Check", NodeCategory.QUALITY, "quality-range",
                        List.of("NATIVE"), "NATIVE", schema("field", "min", "max")),
                new NodeMetadata("REGEX_CHECK", "Regex Check", NodeCategory.QUALITY, "quality-regex",
                        List.of("NATIVE"), "NATIVE", schema("field", "regex")),
                new NodeMetadata("LENGTH_CHECK", "Length Check", NodeCategory.QUALITY, "quality-length",
                        List.of("NATIVE"), "NATIVE", schema("field", "minLength", "maxLength")),
                new NodeMetadata("ENUM_CHECK", "Enum Check", NodeCategory.QUALITY, "quality-enum",
                        List.of("NATIVE"), "NATIVE", schema("field", "values")),
                new NodeMetadata("CUSTOM_SQL_CHECK", "Custom SQL Check", NodeCategory.QUALITY, "quality-sql",
                        List.of("NATIVE"), "NATIVE", schema("condition")),
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
