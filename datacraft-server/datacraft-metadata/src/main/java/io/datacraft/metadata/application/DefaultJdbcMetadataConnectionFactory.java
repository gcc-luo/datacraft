package io.datacraft.metadata.application;

import io.datacraft.datasource.application.JdbcUrlBuilder;
import io.datacraft.datasource.domain.Datasource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DefaultJdbcMetadataConnectionFactory implements JdbcMetadataConnectionFactory {
    private final JdbcUrlBuilder urlBuilder;

    public DefaultJdbcMetadataConnectionFactory(JdbcUrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    @Override
    public Connection open(Datasource datasource, String password) throws SQLException {
        return DriverManager.getConnection(urlBuilder.build(datasource), datasource.username(), password);
    }
}
