package com.datacraft.pipeline.infrastructure;

import com.datacraft.api.pipeline.ExecutionStrategy;
import com.datacraft.api.pipeline.PipelineEdgeRequest;
import com.datacraft.api.pipeline.PipelineNodeRequest;
import com.datacraft.api.pipeline.PipelineRequest;
import com.datacraft.api.pipeline.PipelineStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineMigrationTest {
    @Test
    void migrationDefinesPipelineGraphTablesAndCascadeRelationships() throws IOException {
        Path migration = Path.of("src/main/resources/db/migration/V4__create_pipeline_tables.sql");
        String sql = Files.readString(migration, StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("dc_pipeline"));
        assertTrue(sql.contains("dc_pipeline_node"));
        assertTrue(sql.contains("dc_pipeline_edge"));
        assertTrue(sql.contains("graph_json"));
        assertTrue(sql.contains("on delete cascade"));
        assertTrue(sql.contains("unique (pipeline_id, node_key)"));
        assertTrue(sql.contains("uq_dc_pipeline_edge_identity"));
        assertTrue(sql.contains("created_at"));
        assertTrue(sql.contains("updated_at"));
        assertFalse(sql.contains("password"));
    }

    @Test
    void apiContractsRepresentPipelineGraphAndExecutionDefaults() {
        PipelineRequest request = new PipelineRequest("customer sync", "demo", PipelineStatus.DRAFT,
                ExecutionStrategy.AUTO,
                List.of(new PipelineNodeRequest("source", "DATABASE_SOURCE", "Source", 1D, 2D, "{}", null),
                        new PipelineNodeRequest("sink", "DATABASE_SINK", "Sink", 3D, 4D, "{}", null)),
                List.of(new PipelineEdgeRequest("source", "sink", "out", "in", null)));

        assertEquals(PipelineStatus.DRAFT, request.status());
        assertEquals(ExecutionStrategy.AUTO, request.executionStrategy());
        assertEquals(2, request.nodes().size());
        assertEquals("source", request.edges().get(0).sourceNodeKey());
    }
}
