package com.datacraft.datasource.domain;

public interface JdbcConnectionProvider {
    JdbcConnection open(Long datasourceId);
}
