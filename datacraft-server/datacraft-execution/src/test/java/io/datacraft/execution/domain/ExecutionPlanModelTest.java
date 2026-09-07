package io.datacraft.execution.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionPlanModelTest {
    @Test
    void protectsPlanStageReferenceAndRuntimeCollections() {
        DataReference reference = new DataReference(DataReferenceType.DATABASE_TABLE,
                "datacraft://datasource/1/table/customers", Map.of("datasourceId", 1));
        ExecutionStagePlan stage = new ExecutionStagePlan(0, "NATIVE", List.of("source"),
                List.of(reference), List.of(reference));
        ExecutionPlan plan = new ExecutionPlan(7L, List.of(stage), Map.of("pipelineVersion", 1));

        assertThrows(UnsupportedOperationException.class, () -> plan.stages().add(stage));
        assertThrows(UnsupportedOperationException.class, () -> stage.nodeKeys().add("sink"));
        assertThrows(UnsupportedOperationException.class, () -> reference.metadata().put("password", "x"));
        assertThrows(UnsupportedOperationException.class, () -> plan.runtimeVariables().put("x", "y"));
    }
}
