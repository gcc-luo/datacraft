package io.datacraft.datasource.domain;

public interface DatasourceConnectionTester {
    ConnectionTestOutcome test(Datasource datasource, String password);
}
