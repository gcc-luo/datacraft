package com.datacraft.metadata.domain;

import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.domain.Datasource;

public interface MetadataCollector {
    DatasourceType supports();

    MetadataSnapshot collect(Datasource datasource, String password);
}
