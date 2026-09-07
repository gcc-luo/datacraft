package com.datacraft.quality.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.api.quality.QualityCheckRequest;
import com.datacraft.api.quality.QualityResultResponse;
import com.datacraft.api.quality.QualityRuleRequest;
import com.datacraft.api.quality.QualitySampleResponse;
import com.datacraft.datasource.domain.JdbcConnection;
import com.datacraft.datasource.domain.JdbcConnectionProvider;
import com.datacraft.quality.domain.QualityResult;
import com.datacraft.quality.domain.QualityResultRepository;
import com.datacraft.quality.domain.QualityRuleType;
import com.datacraft.quality.domain.QualitySample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QualityApplicationService {
    private static final int SAMPLE_LIMIT = 1000;

    private final JdbcConnectionProvider connectionProvider;
    private final QualityRuleRegistry ruleRegistry;
    private final QualityResultRepository resultRepository;
    private final SqlSafetyValidator sqlSafetyValidator;
    private final MysqlSqlDialect mysqlDialect;
    private final PostgresqlSqlDialect postgresqlDialect;

    public QualityApplicationService(JdbcConnectionProvider connectionProvider, QualityRuleRegistry ruleRegistry,
                                     QualityResultRepository resultRepository, SqlSafetyValidator sqlSafetyValidator,
                                     MysqlSqlDialect mysqlDialect, PostgresqlSqlDialect postgresqlDialect) {
        this.connectionProvider = connectionProvider;
        this.ruleRegistry = ruleRegistry;
        this.resultRepository = resultRepository;
        this.sqlSafetyValidator = sqlSafetyValidator;
        this.mysqlDialect = mysqlDialect;
        this.postgresqlDialect = postgresqlDialect;
    }

    @Transactional
    public List<QualityResultResponse> check(QualityCheckRequest request) {
        validateRequest(request);
        try (JdbcConnection jdbc = connectionProvider.open(request.datasourceId())) {
            SqlDialect dialect = dialect(jdbc.type());
            String table = quoteTable(request.tableName(), dialect);
            List<QualityResultResponse> responses = new ArrayList<>();
            for (QualityRuleRequest rule : request.rules()) {
                responses.add(runRule(request, rule, table, dialect, jdbc.connection()));
            }
            return List.copyOf(responses);
        } catch (QualityValidationException | QualityRuleNotFoundException exception) {
            throw exception;
        } catch (SQLException | RuntimeException exception) {
            if (exception instanceof QualityExecutionException executionException) throw executionException;
            throw new QualityExecutionException("质量检查执行失败", exception);
        }
    }

    public List<QualityResultResponse> list() {
        return resultRepository.findAll().stream().map(this::toResponse).toList();
    }

    public QualityResultResponse get(Long id) {
        QualityResult result = resultRepository.findById(id).orElseThrow(() -> new QualityResultNotFoundException(id));
        return toResponse(result);
    }

    private QualityResultResponse runRule(QualityCheckRequest request, QualityRuleRequest rule, String table,
                                          SqlDialect dialect, Connection connection) throws SQLException {
        QualityRuleProvider provider = ruleRegistry.get(rule.type());
        provider.validate(rule);
        QualityRuleType type = provider.type();
        String predicate = type == QualityRuleType.UNIQUE_CHECK
                ? uniquePredicate(rule, table, dialect)
                : provider.predicate(rule, dialect);
        List<Object> parameters = provider.parameters(rule);
        Counts counts = count(connection, table, predicate, parameters);
        QualityResult saved = resultRepository.save(new QualityResult(null, request.executionId(), request.nodeExecutionId(), type,
                request.datasourceId(), request.tableName(), rule.field(), counts.totalRows, counts.errorRows,
                counts.totalRows - counts.errorRows, passRate(counts.totalRows, counts.errorRows),
                counts.errorRows == 0 ? "PASS" : "FAILED", Instant.now()));
        for (QualitySample sample : samples(connection, table, predicate, parameters, saved.id())) {
            resultRepository.save(sample);
        }
        return toResponse(saved);
    }

    private Counts count(Connection connection, String table, String predicate, List<Object> parameters) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_rows, COALESCE(SUM(CASE WHEN (" + predicate
                + ") THEN 1 ELSE 0 END), 0) AS error_rows FROM " + table;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("质量检查未返回统计结果");
                return new Counts(resultSet.getLong("total_rows"), resultSet.getLong("error_rows"));
            }
        }
    }

    private List<QualitySample> samples(Connection connection, String table, String predicate, List<Object> parameters,
                                        Long resultId) throws SQLException {
        String sql = "SELECT * FROM " + table + " WHERE (" + predicate + ") LIMIT " + SAMPLE_LIMIT;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                List<QualitySample> samples = new ArrayList<>();
                int index = 0;
                while (resultSet.next() && index < SAMPLE_LIMIT) {
                    Map<String, Object> data = new LinkedHashMap<>();
                    for (int column = 1; column <= metadata.getColumnCount(); column++) {
                        data.put(metadata.getColumnLabel(column), resultSet.getObject(column));
                    }
                    samples.add(new QualitySample(null, resultId, index++, data));
                }
                return samples;
            }
        }
    }

    private String uniquePredicate(QualityRuleRequest rule, String table, SqlDialect dialect) {
        String field = dialect.quoteIdentifier(rule.field());
        return field + " IN (SELECT " + field + " FROM " + table + " GROUP BY " + field + " HAVING COUNT(*) > 1)";
    }

    private void validateRequest(QualityCheckRequest request) {
        if (request == null || request.datasourceId() == null) throw new QualityValidationException("数据源不能为空");
        if (request.tableName() == null || request.tableName().isBlank()) throw new QualityValidationException("表名不能为空");
        if (request.rules().isEmpty()) throw new QualityValidationException("至少需要配置一条质量规则");
        if (request.tableName().contains(";") || request.tableName().contains("--") || request.tableName().contains("/*")) {
            throw new QualityValidationException("表名包含不安全内容");
        }
        for (String part : request.tableName().split("\\.")) {
            if (!part.matches("[A-Za-z_][A-Za-z0-9_$]*")) throw new QualityValidationException("表名格式不合法");
        }
    }

    private String quoteTable(String tableName, SqlDialect dialect) {
        return dialect.quoteTable(tableName);
    }

    private SqlDialect dialect(DatasourceType type) {
        return type == DatasourceType.MYSQL ? mysqlDialect : postgresqlDialect;
    }

    private void bind(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) statement.setObject(index + 1, parameters.get(index));
    }

    private double passRate(long totalRows, long errorRows) {
        return totalRows == 0 ? 1.0 : (double) (totalRows - errorRows) / totalRows;
    }

    private QualityResultResponse toResponse(QualityResult result) {
        List<QualitySampleResponse> samples = resultRepository.findSamples(result.id()).stream()
                .map(sample -> new QualitySampleResponse(sample.id(), sample.sampleIndex(), sample.data())).toList();
        return new QualityResultResponse(result.id(), result.executionId(), result.nodeExecutionId(), result.ruleType().name(),
                result.datasourceId(), result.tableName(), result.fieldName(), result.totalRows(), result.errorRows(),
                result.passRows(), result.passRate(), result.status(), samples, result.createdAt());
    }

    private record Counts(long totalRows, long errorRows) { }
}
