package com.datacraft.metadata.infrastructure.persistence;

import com.datacraft.metadata.domain.Dataset;

import java.util.List;
import java.util.Optional;

public interface DatasetRepository {
    List<Dataset> findAll(Long datasourceId, String schemaName, String keyword);

    Optional<Dataset> findById(Long id);

    void replaceDatasourceSnapshot(Long datasourceId, List<Dataset> datasets);
}
