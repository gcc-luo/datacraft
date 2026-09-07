package io.datacraft.quality.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.datacraft.quality.domain.QualityResult;
import io.datacraft.quality.domain.QualityResultRepository;
import io.datacraft.quality.domain.QualityRuleType;
import io.datacraft.quality.domain.QualitySample;
import io.datacraft.quality.infrastructure.persistence.entity.QualityResultEntity;
import io.datacraft.quality.infrastructure.persistence.entity.QualitySampleEntity;
import io.datacraft.quality.infrastructure.persistence.mapper.QualityResultMapper;
import io.datacraft.quality.infrastructure.persistence.mapper.QualitySampleMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcQualityResultRepository implements QualityResultRepository {
    private final QualityResultMapper resultMapper;
    private final QualitySampleMapper sampleMapper;
    private final ObjectMapper objectMapper;

    public JdbcQualityResultRepository(QualityResultMapper resultMapper, QualitySampleMapper sampleMapper,
                                       ObjectMapper objectMapper) {
        this.resultMapper = resultMapper;
        this.sampleMapper = sampleMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public QualityResult save(QualityResult result) {
        QualityResultEntity entity = new QualityResultEntity();
        entity.setId(result.id()); entity.setExecutionId(result.executionId()); entity.setNodeExecutionId(result.nodeExecutionId());
        entity.setRuleType(result.ruleType().name()); entity.setDatasourceId(result.datasourceId()); entity.setTableName(result.tableName());
        entity.setFieldName(result.fieldName()); entity.setTotalRows(result.totalRows()); entity.setErrorRows(result.errorRows());
        entity.setPassRows(result.passRows()); entity.setPassRate(BigDecimal.valueOf(result.passRate())); entity.setStatus(result.status());
        entity.setCreatedAt(result.createdAt() == null ? Instant.now() : result.createdAt());
        if (entity.getId() == null) resultMapper.insert(entity); else resultMapper.updateById(entity);
        return toDomain(entity);
    }

    @Override
    public QualitySample save(QualitySample sample) {
        QualitySampleEntity entity = new QualitySampleEntity();
        entity.setId(sample.id()); entity.setQualityResultId(sample.qualityResultId()); entity.setSampleIndex(sample.sampleIndex());
        try { entity.setDataJson(objectMapper.writeValueAsString(sample.data())); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("质量样本序列化失败", exception); }
        entity.setCreatedAt(Instant.now()); sampleMapper.insertJson(entity);
        return new QualitySample(entity.getId(), entity.getQualityResultId(), entity.getSampleIndex(), sample.data());
    }

    @Override
    public List<QualityResult> findAll() {
        return resultMapper.selectList(new QueryWrapper<QualityResultEntity>().orderByDesc("created_at").orderByDesc("id"))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<QualityResult> findById(Long id) {
        QualityResultEntity entity = resultMapper.selectById(id);
        return entity == null ? Optional.empty() : Optional.of(toDomain(entity));
    }

    @Override
    public List<QualitySample> findSamples(Long resultId) {
        return sampleMapper.selectByResultId(resultId).stream().map(this::toSample).toList();
    }

    private QualityResult toDomain(QualityResultEntity entity) {
        return new QualityResult(entity.getId(), entity.getExecutionId(), entity.getNodeExecutionId(), QualityRuleType.valueOf(entity.getRuleType()),
                entity.getDatasourceId(), entity.getTableName(), entity.getFieldName(), entity.getTotalRows(), entity.getErrorRows(),
                entity.getPassRows(), entity.getPassRate().doubleValue(), entity.getStatus(), entity.getCreatedAt());
    }

    private QualitySample toSample(QualitySampleEntity entity) {
        try {
            Map<String, Object> data = objectMapper.readValue(entity.getDataJson(), new TypeReference<>() {});
            return new QualitySample(entity.getId(), entity.getQualityResultId(), entity.getSampleIndex(), data);
        } catch (JsonProcessingException exception) { throw new IllegalStateException("质量样本反序列化失败", exception); }
    }
}
