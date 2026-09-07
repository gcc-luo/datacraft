package com.datacraft.metadata.domain;

import java.util.List;
import java.util.Objects;

public record MetadataSnapshot(List<Dataset> datasets) {
    public MetadataSnapshot {
        datasets = datasets == null ? List.of() : List.copyOf(datasets);
    }

    public int schemaCount() {
        return (int) datasets.stream().map(Dataset::schemaName).filter(Objects::nonNull).distinct().count();
    }

    public int fieldCount() {
        return datasets.stream().mapToInt(dataset -> dataset.fields().size()).sum();
    }
}
