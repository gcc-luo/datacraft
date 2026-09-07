package com.datacraft.metadata.application;

import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.domain.Datasource;
import com.datacraft.metadata.domain.MetadataCollector;
import com.datacraft.metadata.domain.MetadataSnapshot;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

@Component
public class PostgresqlMetadataCollector implements MetadataCollector {
    private static final Set<String> SYSTEM_SCHEMAS = Set.of("pg_catalog", "information_schema", "pg_toast");
    private static final String ROW_ESTIMATE_SQL = "SELECT c.reltuples::bigint FROM pg_class c "
            + "JOIN pg_namespace n ON n.oid = c.relnamespace "
            + "WHERE n.nspname = ? AND c.relname = ? AND c.relkind = 'r'";

    private final JdbcMetadataSupport support;

    public PostgresqlMetadataCollector(JdbcMetadataConnectionFactory connections) {
        this.support = new JdbcMetadataSupport(connections);
    }

    @Override
    public DatasourceType supports() {
        return DatasourceType.POSTGRESQL;
    }

    @Override
    public MetadataSnapshot collect(Datasource datasource, String password) {
        return support.collect(datasource, password, null, schema -> !SYSTEM_SCHEMAS.contains(schema),
                this::estimateRows);
    }

    private Long estimateRows(Connection connection, String catalog, String schema, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ROW_ESTIMATE_SQL)) {
            statement.setString(1, schema);
            statement.setString(2, table);
            return JdbcMetadataSupport.readSingleLong(statement);
        }
    }
}
