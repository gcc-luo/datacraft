package com.datacraft.execution.nativeengine;

import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.api.pipeline.ExecutionStrategy;
import com.datacraft.api.pipeline.PipelineStatus;
import com.datacraft.datasource.domain.JdbcConnection;
import com.datacraft.datasource.domain.JdbcConnectionProvider;
import com.datacraft.execution.application.EngineRegistry;
import com.datacraft.execution.application.ExecutionPlanner;
import com.datacraft.execution.domain.EngineHealth;
import com.datacraft.execution.domain.EngineHealthStatus;
import com.datacraft.execution.domain.EngineMetadata;
import com.datacraft.execution.domain.ExecutionEngine;
import com.datacraft.pipeline.application.NodeRegistry;
import com.datacraft.pipeline.domain.Pipeline;
import com.datacraft.pipeline.domain.PipelineEdge;
import com.datacraft.pipeline.domain.PipelineNode;
import com.datacraft.pipeline.domain.PipelineValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NativePipelineExecutorTest {
    private final JdbcConnectionProvider connections = mock(JdbcConnectionProvider.class);
    private final NativePipelineExecutor executor = new NativePipelineExecutor(
            new ExecutionPlanner(new PipelineValidator(), new NodeRegistry(),
                    new EngineRegistry(List.of(new NativeExecutionEngine()))), connections);

    @Test
    void marksObjectMapperConstructorForSpringInjection() throws NoSuchMethodException {
        Constructor<NativePipelineExecutor> constructor = NativePipelineExecutor.class
                .getConstructor(ExecutionPlanner.class, JdbcConnectionProvider.class, com.fasterxml.jackson.databind.ObjectMapper.class);

        assertThat(constructor.isAnnotationPresent(Autowired.class)).isTrue();
    }

    @Test
    void streamsFilteredRowsFromMysqlToPostgresqlInBoundedBatches() throws Exception {
        Connection sourceConnection = mock(Connection.class);
        Connection sinkConnection = mock(Connection.class);
        when(sinkConnection.getAutoCommit()).thenReturn(true);
        Statement sourceStatement = mock(Statement.class);
        PreparedStatement sinkStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(connections.open(1L)).thenReturn(new JdbcConnection(DatasourceType.MYSQL, sourceConnection));
        when(connections.open(2L)).thenReturn(new JdbcConnection(DatasourceType.POSTGRESQL, sinkConnection));
        when(sourceConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
                .thenReturn(sourceStatement);
        when(sourceStatement.executeQuery("SELECT * FROM `customers` WHERE status = 'ACTIVE'"))
                .thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(2);
        when(metadata.getColumnName(1)).thenReturn("id");
        when(metadata.getColumnName(2)).thenReturn("name");
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getObject(1)).thenReturn(1, 2);
        when(resultSet.getObject(2)).thenReturn("Alice", "Bob");
        when(sinkConnection.prepareStatement("INSERT INTO \"customers_clean\" (\"id\", \"name\") VALUES (?, ?)"))
                .thenReturn(sinkStatement);
        when(sinkStatement.executeBatch()).thenReturn(new int[]{1, 1});

        NativeExecutionResult result = executor.execute(pipeline());

        assertThat(result.inputRows()).isEqualTo(2);
        assertThat(result.outputRows()).isEqualTo(2);
        verify(sourceStatement).setFetchSize(500);
        verify(sinkConnection).setAutoCommit(false);
        verify(sinkConnection).commit();
        verify(sinkStatement).setObject(1, 1);
        verify(sinkStatement).setObject(2, "Alice");
    }

    @Test
    void rollsBackSinkWhenBatchWriteFails() throws Exception {
        Connection sourceConnection = mock(Connection.class);
        Connection sinkConnection = mock(Connection.class);
        when(sinkConnection.getAutoCommit()).thenReturn(true);
        Statement sourceStatement = mock(Statement.class);
        PreparedStatement sinkStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metadata = mock(ResultSetMetaData.class);
        when(connections.open(1L)).thenReturn(new JdbcConnection(DatasourceType.MYSQL, sourceConnection));
        when(connections.open(2L)).thenReturn(new JdbcConnection(DatasourceType.POSTGRESQL, sinkConnection));
        when(sourceConnection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
                .thenReturn(sourceStatement);
        when(sourceStatement.executeQuery("SELECT * FROM `customers`"))
                .thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnName(1)).thenReturn("id");
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(1);
        when(sinkConnection.prepareStatement("INSERT INTO \"customers_clean\" (\"id\") VALUES (?)"))
                .thenReturn(sinkStatement);
        when(sinkStatement.executeBatch()).thenThrow(new RuntimeException("write failed"));

        try {
            executor.execute(pipelineWithoutFilter());
        } catch (NativeExecutionException expected) {
            // expected
        }

        verify(sinkConnection).rollback();
    }

    private Pipeline pipeline() {
        return new Pipeline(7L, "customer sync", null, PipelineStatus.DRAFT, 1, ExecutionStrategy.AUTO,
                "{}", 1L, Instant.EPOCH, Instant.EPOCH,
                List.of(source(), filter(), sink()), List.of(
                new PipelineEdge(null, "source", "filter", "out", "in", null),
                new PipelineEdge(null, "filter", "sink", "out", "in", null)));
    }

    private Pipeline pipelineWithoutFilter() {
        return new Pipeline(7L, "customer sync", null, PipelineStatus.DRAFT, 1, ExecutionStrategy.AUTO,
                "{}", 1L, Instant.EPOCH, Instant.EPOCH,
                List.of(source(), sink()), List.of(
                new PipelineEdge(null, "source", "sink", "out", "in", null)));
    }

    private PipelineNode source() {
        return new PipelineNode(null, "source", "DATABASE_SOURCE", "source", 0D, 0D,
                "{\"datasourceId\":1,\"tableName\":\"customers\"}", null);
    }

    private PipelineNode filter() {
        return new PipelineNode(null, "filter", "FILTER", "filter", 0D, 0D,
                "{\"expression\":\"status = 'ACTIVE'\"}", null);
    }

    private PipelineNode sink() {
        return new PipelineNode(null, "sink", "DATABASE_SINK", "sink", 0D, 0D,
                "{\"datasourceId\":2,\"tableName\":\"customers_clean\",\"writeMode\":\"APPEND\"}", null);
    }
}
