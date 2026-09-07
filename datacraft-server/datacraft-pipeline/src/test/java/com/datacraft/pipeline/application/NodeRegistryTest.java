package com.datacraft.pipeline.application;

import com.datacraft.api.pipeline.NodeCategory;
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
        assertEquals("数据库源", registry.get("DATABASE_SOURCE").name());
        assertEquals(NodeCategory.TRANSFORM, registry.get("FILTER").category());
        assertEquals("数据筛选", registry.get("FILTER").name());
        assertEquals(NodeCategory.QUALITY, registry.get("NULL_CHECK").category());
        assertEquals("空值检查", registry.get("NULL_CHECK").name());
        assertEquals("唯一性检查", registry.get("UNIQUE_CHECK").name());
        assertEquals("范围检查", registry.get("RANGE_CHECK").name());
        assertEquals("正则检查", registry.get("REGEX_CHECK").name());
        assertEquals("长度检查", registry.get("LENGTH_CHECK").name());
        assertEquals("枚举检查", registry.get("ENUM_CHECK").name());
        assertEquals(NodeCategory.QUALITY, registry.get("CUSTOM_SQL_CHECK").category());
        assertEquals("自定义 SQL 检查", registry.get("CUSTOM_SQL_CHECK").name());
        assertEquals(NodeCategory.SINK, registry.get("DATABASE_SINK").category());
        assertEquals("数据库输出", registry.get("DATABASE_SINK").name());
        assertTrue(registry.get("DATABASE_SOURCE").supportedEngines().contains("NATIVE"));
    }

    @Test
    void rejectsUnknownNodeType() {
        assertThrows(NodeTypeNotFoundException.class, () -> registry.get("NOPE"));
    }
}
