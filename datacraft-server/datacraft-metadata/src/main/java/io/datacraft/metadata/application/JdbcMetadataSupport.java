package io.datacraft.metadata.application;

import io.datacraft.datasource.domain.Datasource;
import io.datacraft.metadata.domain.Dataset;
import io.datacraft.metadata.domain.DatasetField;
import io.datacraft.metadata.domain.MetadataSnapshot;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class JdbcMetadataSupport {
    private final JdbcMetadataConnectionFactory connectionFactory;

    public JdbcMetadataSupport(JdbcMetadataConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public MetadataSnapshot collect(Datasource datasource, String password, String schemaPattern,
                                    Predicate<String> schemaFilter, RowEstimateQuery rowEstimateQuery) {
        try (Connection connection = connectionFactory.open(datasource, password)) {
            DatabaseMetaData metadata = connection.getMetaData();
            List<Dataset> datasets = new ArrayList<>();
            try (ResultSet tables = metadata.getTables(tableCatalog(datasource), schemaPattern, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String catalog = valueOrEmpty(tables.getString("TABLE_CAT"));
                    String schema = tables.getString("TABLE_SCHEM");
                    if (schema == null && datasource.type().name().equals("MYSQL")) {
                        schema = datasource.databaseName();
                    }
                    String table = tables.getString("TABLE_NAME");
                    if (schema == null || table == null || !schemaFilter.test(schema)) {
                        continue;
                    }
                    Set<String> primaryKeys = readPrimaryKeys(metadata, catalog, schema, table);
                    List<DatasetField> fields = readFields(metadata, catalog, schema, table, primaryKeys);
                    Long estimatedRows = estimateRows(connection, rowEstimateQuery, catalog, schema, table);
                    datasets.add(new Dataset(datasource.id(), catalog, schema, table,
                            tables.getString("REMARKS"), estimatedRows, Instant.now(), fields));
                }
            }
            return new MetadataSnapshot(datasets);
        } catch (SQLException exception) {
            throw new MetadataCollectionException(exception);
        }
    }

    private String tableCatalog(Datasource datasource) {
        return datasource.type().name().equals("MYSQL") ? datasource.databaseName() : null;
    }

    private Set<String> readPrimaryKeys(DatabaseMetaData metadata, String catalog, String schema, String table)
            throws SQLException {
        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet result = metadata.getPrimaryKeys(emptyToNull(catalog), schema, table)) {
            while (result.next()) {
                String field = result.getString("COLUMN_NAME");
                if (field != null) {
                    primaryKeys.add(field);
                }
            }
        }
        return primaryKeys;
    }

    private List<DatasetField> readFields(DatabaseMetaData metadata, String catalog, String schema, String table,
                                          Set<String> primaryKeys) throws SQLException {
        List<DatasetField> fields = new ArrayList<>();
        try (ResultSet result = metadata.getColumns(emptyToNull(catalog), schema, table, "%")) {
            while (result.next()) {
                String fieldName = result.getString("COLUMN_NAME");
                if (fieldName == null) {
                    continue;
                }
                int nullableValue = result.getInt("NULLABLE");
                fields.add(new DatasetField(fieldName, result.getInt("ORDINAL_POSITION"),
                        valueOrEmpty(result.getString("TYPE_NAME")),
                        nullableValue != DatabaseMetaData.columnNoNulls,
                        primaryKeys.contains(fieldName), result.getString("REMARKS")));
            }
        }
        fields.sort(Comparator.comparingInt(DatasetField::ordinalPosition));
        return fields;
    }

    private Long estimateRows(Connection connection, RowEstimateQuery query, String catalog, String schema, String table) {
        try {
            return query.estimate(connection, catalog, schema, table);
        } catch (SQLException ignored) {
            return null;
        }
    }

    public interface RowEstimateQuery {
        Long estimate(Connection connection, String catalog, String schema, String table) throws SQLException;
    }

    public static Long readSingleLong(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            if (!result.next() || result.wasNull()) {
                return null;
            }
            return result.getLong(1);
        }
    }

    public static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
