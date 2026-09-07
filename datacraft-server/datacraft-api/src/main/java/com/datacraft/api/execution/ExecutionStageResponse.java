package com.datacraft.api.execution;

import java.util.List;

public record ExecutionStageResponse(int stageIndex, String engineCode, List<String> nodeKeys,
                                     List<DataReferenceResponse> inputReferences,
                                     List<DataReferenceResponse> outputReferences) {
}
