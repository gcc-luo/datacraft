package com.datacraft.execution.domain;

import java.util.List;
import java.util.Map;

public record ExecutionPlan(Long pipelineId, List<ExecutionStagePlan> stages,
                            Map<String, Object> runtimeVariables) {
    public ExecutionPlan {
        if (pipelineId == null) {
            throw new IllegalArgumentException("pipeline id must not be null");
        }
        stages = List.copyOf(stages == null ? List.of() : stages);
        runtimeVariables = Map.copyOf(runtimeVariables == null ? Map.of() : runtimeVariables);
    }
}
