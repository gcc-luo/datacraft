package com.datacraft.metadata.application;

import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.domain.Datasource;
import com.datacraft.metadata.domain.MetadataCollector;
import com.datacraft.metadata.domain.MetadataSnapshot;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class MysqlMetadataCollector implements MetadataCollector {
    private static final String ROW_ESTIMATE_SQL = "SELECT table_rows FROM information_schema.tables "
            + "WHERE table_schema = ? AND table_name = ?";

    private final JdbcMetadataSupport support;

    public MysqlMetadataCollector(JdbcMetadataConnectionFactory connections) {
        this.support = new JdbcMetadataSupport(connections);
    }

    @Override
    public DatasourceType supports() {
        return DatasourceType.MYSQL;
    }

    @Override
    public MetadataSnapshot collect(Datasource datasource, String password) {
        return support.collect(datasource, password, datasource.databaseName(),
                schema -> datasource.databaseName().equalsIgnoreCase(schema), this::estimateRows);
    }

    private Long estimateRows(Connection connection, String catalog, String schema, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(ROW_ESTIMATE_SQL)) {
            statement.setString(1, schema);
            statement.setString(2, table);
            return JdbcMetadataSupport.readSingleLong(statement);
        }
    }
}
