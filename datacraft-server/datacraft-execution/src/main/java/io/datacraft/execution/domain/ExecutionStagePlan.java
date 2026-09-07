package io.datacraft.execution.domain;

import java.util.List;

public record ExecutionStagePlan(int stageIndex, String engineCode, List<String> nodeKeys,
                                 List<DataReference> inputReferences, List<DataReference> outputReferences) {
    public ExecutionStagePlan {
        if (stageIndex < 0) {
            throw new IllegalArgumentException("stage index must not be negative");
        }
        if (engineCode == null || engineCode.isBlank()) {
            throw new IllegalArgumentException("stage engine code must not be blank");
        }
        nodeKeys = List.copyOf(nodeKeys == null ? List.of() : nodeKeys);
        inputReferences = List.copyOf(inputReferences == null ? List.of() : inputReferences);
        outputReferences = List.copyOf(outputReferences == null ? List.of() : outputReferences);
    }
}
