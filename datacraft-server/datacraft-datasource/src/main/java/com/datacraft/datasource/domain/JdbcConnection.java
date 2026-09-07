package com.datacraft.datasource.domain;

import com.datacraft.api.datasource.DatasourceType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public record JdbcConnection(DatasourceType type, Connection connection) implements AutoCloseable {
    public JdbcConnection {
        Objects.requireNonNull(type, "datasource type must not be null");
        Objects.requireNonNull(connection, "connection must not be null");
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new IllegalStateException("数据源连接关闭失败", exception);
        }
    }
}
