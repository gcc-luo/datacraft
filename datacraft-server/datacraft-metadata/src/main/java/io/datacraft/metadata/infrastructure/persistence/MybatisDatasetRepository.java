package io.datacraft.metadata.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.datacraft.metadata.domain.Dataset;
import io.datacraft.metadata.domain.DatasetField;
import io.datacraft.metadata.infrastructure.persistence.entity.DatasetEntity;
import io.datacraft.metadata.infrastructure.persistence.entity.DatasetFieldEntity;
import io.datacraft.metadata.infrastructure.persistence.mapper.DatasetFieldMapper;
import io.datacraft.metadata.infrastructure.persistence.mapper.DatasetMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class MybatisDatasetRepository implements DatasetRepository {
    private final DatasetMapper datasetMapper;
    private final DatasetFieldMapper fieldMapper;

    public MybatisDatasetRepository(DatasetMapper datasetMapper, DatasetFieldMapper fieldMapper) {
        this.datasetMapper = datasetMapper;
        this.fieldMapper = fieldMapper;
    }

    @Override
    public List<Dataset> findAll(Long datasourceId, String schemaName, String keyword) {
        QueryWrapper<DatasetEntity> query = new QueryWrapper<>();
        if (datasourceId != null) query.eq("datasource_id", datasourceId);
        if (schemaName != null && !schemaName.isBlank()) query.eq("schema_name", schemaName);
        query.orderByAsc("schema_name", "table_name");
        return datasetMapper.selectList(query).stream()
                .map(this::toDomain)
                .map(this::withFields)
                .filter(dataset -> keyword == null || keyword.isBlank() || matches(dataset, keyword))
                .toList();
    }

    @Override
    public Optional<Dataset> findById(Long id) {
        return Optional.ofNullable(datasetMapper.selectById(id)).map(this::toDomain).map(this::withFields);
    }

    @Override
    @Transactional
    public void replaceDatasourceSnapshot(Long datasourceId, List<Dataset> datasets) {
        datasetMapper.delete(new QueryWrapper<DatasetEntity>().eq("datasource_id", datasourceId));
        for (Dataset dataset : datasets) {
            DatasetEntity entity = toEntity(dataset, datasourceId);
            datasetMapper.insert(entity);
            for (DatasetField field : dataset.fields()) {
                fieldMapper.insert(toEntity(field, entity.getId()));
            }
        }
    }

    private Dataset withFields(Dataset dataset) {
        if (dataset.id() == null) return dataset;
        List<DatasetField> fields = fieldMapper.selectList(new QueryWrapper<DatasetFieldEntity>()
                        .eq("dataset_id", dataset.id()).orderByAsc("ordinal_position"))
                .stream().map(this::toDomain).toList();
        return new Dataset(dataset.id(), dataset.datasourceId(), dataset.catalogName(), dataset.schemaName(),
                dataset.tableName(), dataset.tableRemark(), dataset.estimatedRowCount(), dataset.collectedAt(), fields);
    }

    private boolean matches(Dataset dataset, String keyword) {
        String normalized = keyword.toLowerCase();
        return contains(dataset.tableName(), normalized) || contains(dataset.tableRemark(), normalized)
                || dataset.fields().stream().anyMatch(field -> contains(field.fieldName(), normalized)
                || contains(field.fieldRemark(), normalized));
    }

    private boolean contains(String value, String normalized) {
        return value != null && value.toLowerCase().contains(normalized);
    }

    private Dataset toDomain(DatasetEntity entity) {
        return new Dataset(entity.getId(), entity.getDatasourceId(), entity.getCatalogName(), entity.getSchemaName(),
                entity.getTableName(), entity.getTableRemark(), entity.getEstimatedRowCount(), entity.getCollectedAt(), List.of());
    }

    private DatasetField toDomain(DatasetFieldEntity entity) {
        return new DatasetField(entity.getId(), entity.getFieldName(), entity.getOrdinalPosition(), entity.getDataType(),
                Boolean.TRUE.equals(entity.getNullable()), Boolean.TRUE.equals(entity.getPrimaryKey()), entity.getFieldRemark());
    }

    private DatasetEntity toEntity(Dataset dataset, Long datasourceId) {
        DatasetEntity entity = new DatasetEntity();
        entity.setDatasourceId(datasourceId);
        entity.setCatalogName(dataset.catalogName() == null ? "" : dataset.catalogName());
        entity.setSchemaName(dataset.schemaName());
        entity.setTableName(dataset.tableName());
        entity.setTableRemark(dataset.tableRemark());
        entity.setEstimatedRowCount(dataset.estimatedRowCount());
        entity.setCollectedAt(dataset.collectedAt() == null ? Instant.now() : dataset.collectedAt());
        return entity;
    }

    private DatasetFieldEntity toEntity(DatasetField field, Long datasetId) {
        DatasetFieldEntity entity = new DatasetFieldEntity();
        entity.setDatasetId(datasetId);
        entity.setFieldName(field.fieldName());
        entity.setOrdinalPosition(field.ordinalPosition());
        entity.setDataType(field.dataType());
        entity.setNullable(field.nullable());
        entity.setPrimaryKey(field.primaryKey());
        entity.setFieldRemark(field.fieldRemark());
        return entity;
    }
}
