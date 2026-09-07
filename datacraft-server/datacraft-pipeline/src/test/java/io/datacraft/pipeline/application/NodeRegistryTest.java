package io.datacraft.pipeline.application;

import io.datacraft.api.pipeline.NodeCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeRegistryTest {
    private final NodeRegistry registry = new NodeRegistry();

    @Test
    void exposesMetadataOnlyBuiltInNodeTypes() {
        assertEquals(10, registry.list().size());
        assertEquals(NodeCategory.SOURCE, registry.get("DATABASE_SOURCE").category());
        assertEquals(NodeCategory.TRANSFORM, registry.get("FILTER").category());
        assertEquals(NodeCategory.QUALITY, registry.get("NULL_CHECK").category());
        assertEquals(NodeCategory.QUALITY, registry.get("CUSTOM_SQL_CHECK").category());
        assertEquals(NodeCategory.SINK, registry.get("DATABASE_SINK").category());
        assertTrue(registry.get("DATABASE_SOURCE").supportedEngines().contains("NATIVE"));
    }

    @Test
    void rejectsUnknownNodeType() {
        assertThrows(NodeTypeNotFoundException.class, () -> registry.get("NOPE"));
    }
}
