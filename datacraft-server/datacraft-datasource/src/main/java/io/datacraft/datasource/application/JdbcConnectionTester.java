package io.datacraft.datasource.application;

import io.datacraft.datasource.domain.ConnectionTestOutcome;
import io.datacraft.datasource.domain.Datasource;
import io.datacraft.datasource.domain.DatasourceConnectionTester;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class JdbcConnectionTester implements DatasourceConnectionTester {
    private final JdbcUrlBuilder urlBuilder;

    public JdbcConnectionTester(JdbcUrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
    }

    @Override
    public ConnectionTestOutcome test(Datasource datasource, String password) {
        long startedAt = System.nanoTime();
        try (Connection ignored = DriverManager.getConnection(urlBuilder.build(datasource), datasource.username(), password)) {
            return ConnectionTestOutcome.success(elapsedMillis(startedAt));
        } catch (SQLException exception) {
            return ConnectionTestOutcome.failure(elapsedMillis(startedAt));
        }
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }
}
