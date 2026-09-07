package io.datacraft.execution.application;

import io.datacraft.api.pipeline.ExecutionStrategy;
import io.datacraft.execution.domain.DataReferenceType;
import io.datacraft.execution.domain.EngineHealth;
import io.datacraft.execution.domain.EngineHealthStatus;
import io.datacraft.execution.domain.EngineMetadata;
import io.datacraft.execution.domain.ExecutionEngine;
import io.datacraft.execution.domain.ExecutionPlan;
import io.datacraft.execution.nativeengine.NativeExecutionEngine;
import io.datacraft.pipeline.application.NodeRegistry;
import io.datacraft.pipeline.domain.Pipeline;
import io.datacraft.pipeline.domain.PipelineEdge;
import io.datacraft.pipeline.domain.PipelineNode;
import io.datacraft.pipeline.domain.PipelineValidator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionPlannerTest {
    private final NodeRegistry nodeRegistry = new NodeRegistry();
    private final ExecutionPlanner planner = new ExecutionPlanner(new PipelineValidator(), nodeRegistry,
            new EngineRegistry(List.of(new NativeExecutionEngine(), testEngine("DATAX"))));

    @Test
    void plansDefaultDatabaseFilterPipelineAsOneNativeStage() {
        Pipeline pipeline = pipeline(7L, ExecutionStrategy.AUTO,
                List.of(source("source", null), filter("filter", null), sink("sink", null)),
                List.of(edge("source", "filter"), edge("filter", "sink")));

        ExecutionPlan plan = planner.plan(pipeline);

        assertEquals(7L, plan.pipelineId());
        assertEquals(1, plan.stages().size());
        assertEquals("NATIVE", plan.stages().get(0).engineCode());
        assertEquals(List.of("source", "filter", "sink"), plan.stages().get(0).nodeKeys());
        assertEquals(DataReferenceType.DATABASE_TABLE,
                plan.stages().get(0).inputReferences().get(0).type());
        assertEquals(DataReferenceType.DATABASE_TABLE,
                plan.stages().get(0).outputReferences().get(0).type());
        assertTrue(plan.stages().get(0).inputReferences().get(0).metadata().containsKey("datasourceId"));
        assertTrue(plan.stages().get(0).outputReferences().get(0).metadata().containsKey("tableName"));
    }

    @Test
    void splitsConsecutiveEngineSegmentsAndAddsMaterializationReference() {
        Pipeline pipeline = pipeline(9L, ExecutionStrategy.AUTO,
                List.of(source("source", "NATIVE"), sink("sink", "DATAX")),
                List.of(edge("source", "sink")));

        ExecutionPlan plan = planner.plan(pipeline);

        assertEquals(List.of("NATIVE", "DATAX"), plan.stages().stream()
                .map(stage -> stage.engineCode()).toList());
        assertEquals(DataReferenceType.DATABASE_QUERY, plan.stages().get(1).inputReferences().get(0).type());
        assertEquals("MATERIALIZATION_REQUIRED",
                plan.stages().get(1).inputReferences().get(0).metadata().get("handoff"));
    }

    @Test
    void rejectsUnsupportedEngineBeforeCreatingPlan() {
        Pipeline pipeline = pipeline(7L, ExecutionStrategy.AUTO,
                List.of(source("source", null), filter("filter", "DATAX"), sink("sink", null)),
                List.of(edge("source", "filter"), edge("filter", "sink")));

        assertThrows(ExecutionPlanningException.class, () -> planner.plan(pipeline));
    }

    @Test
    void rejectsInvalidDatabaseNodeConfiguration() {
        Pipeline pipeline = pipeline(7L, ExecutionStrategy.AUTO,
                List.of(new PipelineNode(null, "source", "DATABASE_SOURCE", "source", 0D, 0D,
                        "{\"datasourceId\":1}", null)), List.of());

        assertThrows(ExecutionPlanningException.class, () -> planner.plan(pipeline));
    }

    private ExecutionEngine testEngine(String code) {
        return new ExecutionEngine() {
            @Override public String engineType() { return code; }
            @Override public EngineMetadata metadata() { return EngineMetadata.test(code); }
            @Override public EngineHealth healthCheck() {
                return new EngineHealth(EngineHealthStatus.UP, "ok", Instant.EPOCH);
            }
        };
    }

    private Pipeline pipeline(Long id, ExecutionStrategy strategy, List<PipelineNode> nodes,
                              List<PipelineEdge> edges) {
        Instant now = Instant.parse("2026-09-07T00:00:00Z");
        return new Pipeline(id, "customer sync", null, io.datacraft.api.pipeline.PipelineStatus.DRAFT,
                1, strategy, "{}", 1L, now, now, nodes, edges);
    }

    private PipelineNode source(String key, String preferredEngine) {
        return new PipelineNode(null, key, "DATABASE_SOURCE", key, 0D, 0D,
                "{\"datasourceId\":1,\"tableName\":\"customers\"}", preferredEngine);
    }

    private PipelineNode filter(String key, String preferredEngine) {
        return new PipelineNode(null, key, "FILTER", key, 0D, 0D,
                "{\"expression\":\"status = 'ACTIVE'\"}", preferredEngine);
    }

    private PipelineNode sink(String key, String preferredEngine) {
        return new PipelineNode(null, key, "DATABASE_SINK", key, 0D, 0D,
                "{\"datasourceId\":2,\"tableName\":\"customers_clean\",\"writeMode\":\"UPSERT\"}", preferredEngine);
    }

    private PipelineEdge edge(String source, String target) {
        return new PipelineEdge(null, source, target, "out", "in", null);
    }
}
