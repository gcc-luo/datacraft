package io.datacraft.metadata.domain;

import io.datacraft.api.datasource.DatasourceType;
import io.datacraft.datasource.domain.Datasource;

public interface MetadataCollector {
    DatasourceType supports();

    MetadataSnapshot collect(Datasource datasource, String password);
}
