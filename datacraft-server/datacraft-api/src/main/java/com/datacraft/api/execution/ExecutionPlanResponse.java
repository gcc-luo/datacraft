package com.datacraft.api.execution;

import java.util.List;

public record ExecutionPlanResponse(Long pipelineId, List<ExecutionStageResponse> stages) {
}
