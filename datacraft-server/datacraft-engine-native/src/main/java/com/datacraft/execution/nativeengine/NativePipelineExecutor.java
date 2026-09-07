package com.datacraft.execution.nativeengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.domain.JdbcConnection;
import com.datacraft.datasource.domain.JdbcConnectionProvider;
import com.datacraft.execution.application.ExecutionPlanner;
import com.datacraft.execution.domain.ExecutionPlan;
import com.datacraft.execution.domain.ExecutionStagePlan;
import com.datacraft.pipeline.domain.Pipeline;
import com.datacraft.pipeline.domain.PipelineNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class NativePipelineExecutor {
    private static final int BATCH_SIZE = 500;

    private final ExecutionPlanner planner;
    private final JdbcConnectionProvider connections;
    private final ObjectMapper objectMapper;

    public NativePipelineExecutor(ExecutionPlanner planner, JdbcConnectionProvider connections) {
        this(planner, connections, new ObjectMapper());
    }

    @Autowired
    public NativePipelineExecutor(ExecutionPlanner planner, JdbcConnectionProvider connections,
                                  ObjectMapper objectMapper) {
        this.planner = planner;
        this.connections = connections;
        this.objectMapper = objectMapper;
    }

    public NativeExecutionResult execute(Pipeline pipeline) {
        ExecutionPlan plan = planner.plan(pipeline);
        if (plan.stages().size() != 1 || !"NATIVE".equals(plan.stages().get(0).engineCode())) {
            throw new NativeExecutionException("Native Runtime 仅支持单 Native Stage");
        }
        ExecutionStagePlan stage = plan.stages().get(0);
        Map<String, PipelineNode> nodesByKey = (pipeline.nodes() == null ? List.<PipelineNode>of() : pipeline.nodes())
                .stream().collect(java.util.stream.Collectors.toMap(PipelineNode::nodeKey, node -> node));
        List<PipelineNode> orderedNodes = stage.nodeKeys().stream().map(nodesByKey::get).toList();
        PipelineNode source = singleNode(orderedNodes, "DATABASE_SOURCE", "源");
        PipelineNode sink = singleNode(orderedNodes, "DATABASE_SINK", "目标");
        List<String> filters = orderedNodes.stream().filter(node -> "FILTER".equals(node.nodeType()))
                .map(this::filterExpression).toList();
        DatabaseNodeConfig sourceConfig = databaseConfig(source);
        DatabaseNodeConfig sinkConfig = databaseConfig(sink);
        validateWriteMode(sinkConfig.writeMode());

        try (JdbcConnection sourceConnection = connections.open(sourceConfig.datasourceId());
             JdbcConnection sinkConnection = connections.open(sinkConfig.datasourceId())) {
            String selectSql = selectSql(sourceConnection.type(), sourceConfig.tableName(), filters);
            try (Statement sourceStatement = sourceConnection.connection().createStatement(
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                sourceStatement.setFetchSize(BATCH_SIZE);
                try (ResultSet resultSet = sourceStatement.executeQuery(selectSql)) {
                    return writeRows(resultSet, sinkConnection, sinkConfig);
                }
            }
        } catch (NativeExecutionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new NativeExecutionException("Native Pipeline 执行失败", exception);
        }
    }

    private NativeExecutionResult writeRows(ResultSet resultSet, JdbcConnection sinkConnection,
                                            DatabaseNodeConfig sinkConfig)
            throws SQLException {
        if ("TRUNCATE".equals(sinkConfig.writeMode())) {
            try (Statement truncate = sinkConnection.connection().createStatement()) {
                truncate.executeUpdate("TRUNCATE TABLE " + quote(sinkConnection.type(), sinkConfig.tableName()));
            }
        }
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        if (columnCount == 0) {
            throw new NativeExecutionException("源数据没有可写入的列");
        }
        List<String> columnNames = new ArrayList<>();
        for (int index = 1; index <= columnCount; index++) {
            String column = metadata.getColumnName(index);
            if (column == null || column.isBlank()) {
                throw new NativeExecutionException("源数据存在空列名");
            }
            columnNames.add(column);
        }
        String insertSql = insertSql(sinkConnection.type(), sinkConfig.tableName(), columnNames);
        Connection sink = sinkConnection.connection();
        boolean previousAutoCommit = sink.getAutoCommit();
        if (previousAutoCommit) {
            sink.setAutoCommit(false);
        }
        long inputRows = 0;
        long outputRows = 0;
        try (PreparedStatement insert = sink.prepareStatement(insertSql)) {
            int batchRows = 0;
            while (resultSet.next()) {
                inputRows++;
                for (int index = 1; index <= columnCount; index++) {
                    insert.setObject(index, resultSet.getObject(index));
                }
                insert.addBatch();
                batchRows++;
                if (batchRows == BATCH_SIZE) {
                    outputRows += executeBatch(insert, batchRows);
                    batchRows = 0;
                }
            }
            if (batchRows > 0) {
                outputRows += executeBatch(insert, batchRows);
            }
            sink.commit();
            if (previousAutoCommit) {
                sink.setAutoCommit(true);
            }
            return new NativeExecutionResult(inputRows, outputRows);
        } catch (Exception exception) {
            try {
                sink.rollback();
            } finally {
                if (previousAutoCommit) {
                    sink.setAutoCommit(true);
                }
            }
            if (exception instanceof NativeExecutionException nativeException) {
                throw nativeException;
            }
            throw new NativeExecutionException("目标数据写入失败", exception);
        }
    }

    private long executeBatch(PreparedStatement insert, int batchRows) throws SQLException {
        int[] results = insert.executeBatch();
        long outputRows = 0;
        for (int result : results) {
            if (result == Statement.EXECUTE_FAILED) {
                throw new NativeExecutionException("目标数据批量写入失败");
            }
            outputRows += result == Statement.SUCCESS_NO_INFO ? 1 : result;
        }
        return results.length == 0 ? batchRows : outputRows;
    }

    private PipelineNode singleNode(List<PipelineNode> nodes, String type, String label) {
        List<PipelineNode> matches = nodes.stream().filter(node -> type.equals(node.nodeType())).toList();
        if (matches.size() != 1) {
            throw new NativeExecutionException("Native Runtime 需要且只能有一个" + label + "节点");
        }
        return matches.get(0);
    }

    private DatabaseNodeConfig databaseConfig(PipelineNode node) {
        JsonNode config = parseConfig(node);
        JsonNode datasourceId = config.get("datasourceId");
        JsonNode tableName = config.get("tableName");
        if (datasourceId == null || !datasourceId.canConvertToLong() || tableName == null
                || tableName.asText().isBlank()) {
            throw new NativeExecutionException("节点 " + node.nodeKey() + " 缺少有效 datasourceId 或 tableName");
        }
        String writeMode = config.hasNonNull("writeMode") && !config.get("writeMode").asText().isBlank()
                ? config.get("writeMode").asText().trim().toUpperCase(Locale.ROOT) : "APPEND";
        return new DatabaseNodeConfig(datasourceId.longValue(), tableName.asText(), writeMode);
    }

    private String filterExpression(PipelineNode node) {
        JsonNode config = parseConfig(node);
        JsonNode expression = config.get("expression");
        if (expression == null || expression.asText().isBlank()) {
            throw new NativeExecutionException("过滤节点 " + node.nodeKey() + " 缺少 expression");
        }
        String value = expression.asText().trim();
        if (value.contains(";") || value.contains("--") || value.contains("/*") || value.contains("*/")) {
            throw new NativeExecutionException("过滤表达式包含不允许的 SQL 分隔符");
        }
        return value;
    }

    private JsonNode parseConfig(PipelineNode node) {
        try {
            JsonNode config = objectMapper.readTree(node.configJson() == null ? "{}" : node.configJson());
            if (config == null || !config.isObject()) {
                throw new NativeExecutionException("节点 " + node.nodeKey() + " 配置必须是 JSON 对象");
            }
            return config;
        } catch (JsonProcessingException exception) {
            throw new NativeExecutionException("节点 " + node.nodeKey() + " 配置不是合法 JSON", exception);
        }
    }

    private void validateWriteMode(String writeMode) {
        if (!"APPEND".equals(writeMode) && !"TRUNCATE".equals(writeMode)) {
            throw new NativeExecutionException("Native Runtime 不支持写入模式: " + writeMode);
        }
    }

    private String selectSql(DatasourceType type, String tableName, List<String> filters) {
        String sql = "SELECT * FROM " + quote(type, tableName);
        return filters.isEmpty() ? sql : sql + " WHERE " + String.join(" AND ", filters);
    }

    private String insertSql(DatasourceType type, String tableName, List<String> columnNames) {
        String columns = columnNames.stream().map(column -> quote(type, column)).collect(java.util.stream.Collectors.joining(", "));
        String placeholders = "?, ".repeat(Math.max(0, columnNames.size() - 1)) + "?";
        return "INSERT INTO " + quote(type, tableName) + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    private String quote(DatasourceType type, String identifier) {
        String[] parts = identifier.split("\\.", -1);
        if (parts.length == 0 || java.util.Arrays.stream(parts).anyMatch(String::isBlank)) {
            throw new NativeExecutionException("数据库标识符不能为空");
        }
        String quote = type == DatasourceType.MYSQL ? "`" : "\"";
        String escapedQuote = quote + quote;
        return java.util.Arrays.stream(parts)
                .map(part -> quote + part.replace(quote, escapedQuote) + quote)
                .collect(java.util.stream.Collectors.joining("."));
    }

    private record DatabaseNodeConfig(Long datasourceId, String tableName, String writeMode) {
    }
}
