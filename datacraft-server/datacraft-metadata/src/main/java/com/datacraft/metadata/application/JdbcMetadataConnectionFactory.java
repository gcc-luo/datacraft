package com.datacraft.metadata.application;

import com.datacraft.datasource.domain.Datasource;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface JdbcMetadataConnectionFactory {
    Connection open(Datasource datasource, String password) throws SQLException;
}
