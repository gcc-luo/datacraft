package io.datacraft.metadata.application;

import io.datacraft.api.datasource.DatasourceStatus;
import io.datacraft.api.datasource.DatasourceType;
import io.datacraft.datasource.domain.Datasource;
import io.datacraft.metadata.domain.Dataset;
import io.datacraft.metadata.domain.MetadataSnapshot;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostgresqlMetadataCollectorTest {
    @Test
    void collectsTablesFieldsPrimaryKeysAndEstimatesButSkipsSystemSchema() throws Exception {
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
        when(metadata.getTables(null, null, "%", new String[]{"TABLE"})).thenReturn(tables);
        when(tables.next()).thenReturn(true, true, false);
        when(tables.getString("TABLE_CAT")).thenReturn("datacraft", "datacraft");
        when(tables.getString("TABLE_SCHEM")).thenReturn("pg_catalog", "public");
        when(tables.getString("TABLE_NAME")).thenReturn("pg_type", "customer");
        when(tables.getString("REMARKS")).thenReturn(null, "客户表");
        when(metadata.getColumns("datacraft", "public", "customer", "%")).thenReturn(columns);
        when(columns.next()).thenReturn(true, false);
        when(columns.getString("COLUMN_NAME")).thenReturn("id");
        when(columns.getInt("ORDINAL_POSITION")).thenReturn(1);
        when(columns.getString("TYPE_NAME")).thenReturn("int8");
        when(columns.getInt("NULLABLE")).thenReturn(DatabaseMetaData.columnNoNulls);
        when(columns.getString("REMARKS")).thenReturn("主键");
        when(metadata.getPrimaryKeys("datacraft", "public", "customer")).thenReturn(primaryKeys);
        when(primaryKeys.next()).thenReturn(true, false);
        when(primaryKeys.getString("COLUMN_NAME")).thenReturn("id");
        when(connection.prepareStatement(anyString())).thenReturn(estimateStatement);
        when(estimateStatement.executeQuery()).thenReturn(estimate);
        when(estimate.next()).thenReturn(true);
        when(estimate.getLong(1)).thenReturn(42L);

        MetadataSnapshot snapshot = new PostgresqlMetadataCollector(connections).collect(datasource, "secret");

        assertEquals(1, snapshot.datasets().size());
        Dataset dataset = snapshot.datasets().get(0);
        assertEquals("public", dataset.schemaName());
        assertEquals("customer", dataset.tableName());
        assertEquals(42L, dataset.estimatedRowCount());
        assertEquals("id", dataset.fields().get(0).fieldName());
        assertTrue(dataset.fields().get(0).primaryKey());
        assertFalse(dataset.fields().get(0).nullable());
        verify(connections).open(datasource, "secret");
        verify(tables).close();
        verify(columns).close();
        verify(primaryKeys).close();
        verify(estimate).close();
        verify(estimateStatement).close();
        verify(connection).close();
    }

    @Test
    void keepsTableWhenRowEstimateFails() throws Exception {
        JdbcMetadataConnectionFactory connections = mock(JdbcMetadataConnectionFactory.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        ResultSet tables = mock(ResultSet.class);
        ResultSet columns = mock(ResultSet.class);
        ResultSet primaryKeys = mock(ResultSet.class);
        PreparedStatement estimateStatement = mock(PreparedStatement.class);
        Datasource datasource = datasource();
        when(connections.open(datasource, "secret")).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getTables(null, null, "%", new String[]{"TABLE"})).thenReturn(tables);
        when(tables.next()).thenReturn(true, false);
        when(tables.getString("TABLE_CAT")).thenReturn("datacraft");
        when(tables.getString("TABLE_SCHEM")).thenReturn("public");
        when(tables.getString("TABLE_NAME")).thenReturn("customer");
        when(metadata.getColumns("datacraft", "public", "customer", "%")).thenReturn(columns);
        when(columns.next()).thenReturn(false);
        when(metadata.getPrimaryKeys("datacraft", "public", "customer")).thenReturn(primaryKeys);
        when(primaryKeys.next()).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenReturn(estimateStatement);
        when(estimateStatement.executeQuery()).thenThrow(new java.sql.SQLException("hidden"));

        Dataset dataset = new PostgresqlMetadataCollector(connections).collect(datasource, "secret").datasets().get(0);

        assertNull(dataset.estimatedRowCount());
        assertEquals("customer", dataset.tableName());
    }

    private Datasource datasource() {
        Instant now = Instant.parse("2026-09-07T03:00:00Z");
        return new Datasource(1L, "warehouse", DatasourceType.POSTGRESQL, "localhost", 5432, "datacraft",
                "reader", "v1:cipher", null, DatasourceStatus.UNKNOWN, null, null, null, now, now);
    }

    private void assertTrue(boolean value) {
        org.junit.jupiter.api.Assertions.assertTrue(value);
    }
}
