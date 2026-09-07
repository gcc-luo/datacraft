package io.datacraft.metadata.application;

import io.datacraft.api.datasource.DatasourceStatus;
import io.datacraft.api.datasource.DatasourceType;
import io.datacraft.datasource.domain.Datasource;
import io.datacraft.metadata.domain.MetadataSnapshot;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MysqlMetadataCollectorTest {
    @Test
    void limitsCollectionToConfiguredDatabaseSchema() throws Exception {
        JdbcMetadataConnectionFactory connections = mock(JdbcMetadataConnectionFactory.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet tables = mock(ResultSet.class);
        ResultSet columns = mock(ResultSet.class);
        ResultSet primaryKeys = mock(ResultSet.class);
        PreparedStatement estimateStatement = mock(PreparedStatement.class);
        ResultSet estimate = mock(ResultSet.class);
        Datasource datasource = datasource();
        when(connections.open(datasource, "secret")).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getTables("analytics", "analytics", "%", new String[]{"TABLE"})).thenReturn(tables);
        when(tables.next()).thenReturn(true, true, false);
        when(tables.getString("TABLE_CAT")).thenReturn("analytics", "analytics");
        when(tables.getString("TABLE_SCHEM")).thenReturn("other", "analytics");
        when(tables.getString("TABLE_NAME")).thenReturn("ignored", "customer");
        when(tables.getString("REMARKS")).thenReturn(null, null);
        when(metadata.getColumns("analytics", "analytics", "customer", "%")).thenReturn(columns);
        when(columns.next()).thenReturn(false);
        when(metadata.getPrimaryKeys("analytics", "analytics", "customer")).thenReturn(primaryKeys);
        when(primaryKeys.next()).thenReturn(false);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(estimateStatement);
        when(estimateStatement.executeQuery()).thenReturn(estimate);
        when(estimate.next()).thenReturn(true);
        when(estimate.getLong(1)).thenReturn(24L);

        MetadataSnapshot snapshot = new MysqlMetadataCollector(connections).collect(datasource, "secret");

        assertEquals(1, snapshot.datasets().size());
        assertEquals("analytics", snapshot.datasets().get(0).schemaName());
        assertEquals("customer", snapshot.datasets().get(0).tableName());
        assertEquals(24L, snapshot.datasets().get(0).estimatedRowCount());
        verify(metadata).getTables("analytics", "analytics", "%", new String[]{"TABLE"});
    }

    @Test
    void usesConfiguredDatabaseWhenDriverLeavesTableSchemaNull() throws Exception {
        JdbcMetadataConnectionFactory connections = mock(JdbcMetadataConnectionFactory.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet tables = mock(ResultSet.class);
        ResultSet columns = mock(ResultSet.class);
        ResultSet primaryKeys = mock(ResultSet.class);
        PreparedStatement estimateStatement = mock(PreparedStatement.class);
        ResultSet estimate = mock(ResultSet.class);
        Datasource datasource = datasource();
        when(connections.open(datasource, "secret")).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getTables("analytics", "analytics", "%", new String[]{"TABLE"})).thenReturn(tables);
        when(tables.next()).thenReturn(true, false);
        when(tables.getString("TABLE_CAT")).thenReturn("analytics");
        when(tables.getString("TABLE_SCHEM")).thenReturn(null);
        when(tables.getString("TABLE_NAME")).thenReturn("customer");
        when(tables.getString("REMARKS")).thenReturn(null);
        when(metadata.getColumns("analytics", "analytics", "customer", "%")).thenReturn(columns);
        when(columns.next()).thenReturn(false);
        when(metadata.getPrimaryKeys("analytics", "analytics", "customer")).thenReturn(primaryKeys);
        when(primaryKeys.next()).thenReturn(false);
        when(connection.prepareStatement(org.mockito.ArgumentMatchers.anyString())).thenReturn(estimateStatement);
        when(estimateStatement.executeQuery()).thenReturn(estimate);
        when(estimate.next()).thenReturn(true);
        when(estimate.getLong(1)).thenReturn(24L);

        MetadataSnapshot snapshot = new MysqlMetadataCollector(connections).collect(datasource, "secret");

        assertEquals(1, snapshot.datasets().size());
        assertEquals("analytics", snapshot.datasets().get(0).schemaName());
    }

    private Datasource datasource() {
        Instant now = Instant.parse("2026-09-07T03:00:00Z");
        return new Datasource(2L, "mysql", DatasourceType.MYSQL, "localhost", 3306, "analytics",
                "reader", "v1:cipher", null, DatasourceStatus.UNKNOWN, null, null, null, now, now);
    }
}
