package io.datacraft.datasource.domain;

public interface JdbcConnectionProvider {
    JdbcConnection open(Long datasourceId);
}
