package io.datacraft.quality.application;

import io.datacraft.api.datasource.DatasourceType;
import io.datacraft.api.quality.QualityCheckRequest;
import io.datacraft.api.quality.QualityResultResponse;
import io.datacraft.api.quality.QualityRuleRequest;
import io.datacraft.datasource.domain.JdbcConnection;
import io.datacraft.datasource.domain.JdbcConnectionProvider;
import io.datacraft.quality.domain.QualityResult;
import io.datacraft.quality.domain.QualityResultRepository;
import io.datacraft.quality.domain.QualityRuleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualityApplicationServiceTest {
    @Mock JdbcConnectionProvider connectionProvider;
    @Mock QualityResultRepository resultRepository;
    @Mock Connection connection;
    @Mock PreparedStatement countStatement;
    @Mock PreparedStatement sampleStatement;
    @Mock ResultSet countResult;
    @Mock ResultSet sampleResult;

    @Test
    void calculatesPassRateAndPersistsResult() throws Exception {
        when(connectionProvider.open(1L)).thenReturn(new JdbcConnection(DatasourceType.POSTGRESQL, connection));
        when(connection.prepareStatement(any(String.class))).thenReturn(countStatement, sampleStatement);
        when(countStatement.executeQuery()).thenReturn(countResult);
        when(countResult.next()).thenReturn(true);
        when(countResult.getLong("total_rows")).thenReturn(10L);
        when(countResult.getLong("error_rows")).thenReturn(2L);
        when(sampleStatement.executeQuery()).thenReturn(sampleResult);
        when(sampleResult.next()).thenReturn(false);
        QualityResult saved = new QualityResult(7L, null, null, QualityRuleType.NULL_CHECK, 1L, "customer", "email",
                10L, 2L, 8L, 0.8, "FAILED", Instant.now());
        when(resultRepository.save(any(QualityResult.class))).thenReturn(saved);
        when(resultRepository.findSamples(7L)).thenReturn(List.of());

        QualityApplicationService service = new QualityApplicationService(connectionProvider, new QualityRuleRegistry(),
                resultRepository, new SqlSafetyValidator(), new MysqlSqlDialect(), new PostgresqlSqlDialect());
        QualityCheckRequest request = new QualityCheckRequest(1L, null, null, "customer",
                List.of(new QualityRuleRequest("NULL_CHECK", "email", null, null, null, null, null, List.of(), null)));

        QualityResultResponse response = service.check(request).get(0);

        assertEquals(10L, response.totalRows());
        assertEquals(2L, response.errorRows());
        assertEquals(8L, response.passRows());
        assertEquals(0.8, response.passRate());
        assertEquals("FAILED", response.status());
        verify(resultRepository).save(any(QualityResult.class));
    }

    @Test
    void capsPersistedErrorSamplesAtOneThousand() throws Exception {
        ResultSetMetaData metadata = org.mockito.Mockito.mock(ResultSetMetaData.class);
        when(connectionProvider.open(1L)).thenReturn(new JdbcConnection(DatasourceType.POSTGRESQL, connection));
        when(connection.prepareStatement(any(String.class))).thenReturn(countStatement, sampleStatement);
        when(countStatement.executeQuery()).thenReturn(countResult);
        when(countResult.next()).thenReturn(true);
        when(countResult.getLong("total_rows")).thenReturn(1001L);
        when(countResult.getLong("error_rows")).thenReturn(1001L);
        when(sampleStatement.executeQuery()).thenReturn(sampleResult);
        when(sampleResult.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(1);
        when(metadata.getColumnLabel(1)).thenReturn("id");
        final int[] calls = { 0 };
        when(sampleResult.next()).thenAnswer(invocation -> calls[0]++ < 1001);
        when(sampleResult.getObject(1)).thenReturn(1L);
        QualityResult saved = new QualityResult(8L, null, null, QualityRuleType.NULL_CHECK, 1L, "customer", "email",
                1001L, 1001L, 0L, 0.0, "FAILED", Instant.now());
        when(resultRepository.save(any(QualityResult.class))).thenReturn(saved);
        when(resultRepository.findSamples(8L)).thenReturn(List.of());

        QualityApplicationService service = new QualityApplicationService(connectionProvider, new QualityRuleRegistry(),
                resultRepository, new SqlSafetyValidator(), new MysqlSqlDialect(), new PostgresqlSqlDialect());
        QualityCheckRequest request = new QualityCheckRequest(1L, null, null, "customer",
                List.of(new QualityRuleRequest("NULL_CHECK", "email", null, null, null, null, null, List.of(), null)));

        service.check(request);

        verify(resultRepository, times(1000)).save(any(io.datacraft.quality.domain.QualitySample.class));
    }
}
