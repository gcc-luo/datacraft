package com.datacraft.metadata.application;

import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.api.metadata.DatasetDetailResponse;
import com.datacraft.api.metadata.DatasetFieldResponse;
import com.datacraft.api.metadata.DatasetResponse;
import com.datacraft.api.metadata.MetadataSyncResponse;
import com.datacraft.datasource.application.DatasourceNotFoundException;
import com.datacraft.datasource.domain.Datasource;
import com.datacraft.datasource.domain.DatasourceRepository;
import com.datacraft.datasource.security.SecretCryptoService;
import com.datacraft.metadata.domain.Dataset;
import com.datacraft.metadata.domain.DatasetField;
import com.datacraft.metadata.domain.MetadataCollector;
import com.datacraft.metadata.domain.MetadataSnapshot;
import com.datacraft.metadata.infrastructure.persistence.DatasetRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class MetadataApplicationService {
    private final DatasourceRepository datasourceRepository;
    private final SecretCryptoService crypto;
    private final DatasetRepository datasetRepository;
    private final Map<DatasourceType, MetadataCollector> collectors;

    public MetadataApplicationService(DatasourceRepository datasourceRepository, SecretCryptoService crypto,
                                      DatasetRepository datasetRepository, List<MetadataCollector> collectors) {
        this.datasourceRepository = datasourceRepository;
        this.crypto = crypto;
        this.datasetRepository = datasetRepository;
        this.collectors = new EnumMap<>(DatasourceType.class);
        collectors.forEach(collector -> this.collectors.put(collector.supports(), collector));
    }

    public MetadataSyncResponse sync(Long datasourceId) {
        Datasource datasource = datasourceRepository.findById(datasourceId)
                .orElseThrow(() -> new DatasourceNotFoundException(datasourceId));
        MetadataCollector collector = collectors.get(datasource.type());
        if (collector == null) throw new MetadataCollectionException();
        String password = crypto.decrypt(datasource.passwordCiphertext());
        MetadataSnapshot snapshot = collector.collect(datasource, password);
        datasetRepository.replaceDatasourceSnapshot(datasourceId, snapshot.datasets());
        return new MetadataSyncResponse(datasourceId, snapshot.schemaCount(), snapshot.datasets().size(),
                snapshot.fieldCount(), Instant.now());
    }

    public List<DatasetResponse> list(Long datasourceId, String schemaName, String keyword) {
        return datasetRepository.findAll(datasourceId, schemaName, keyword).stream().map(this::toResponse).toList();
    }

    public DatasetDetailResponse get(Long id) {
        Dataset dataset = datasetRepository.findById(id).orElseThrow(() -> new DatasetNotFoundException(id));
        return new DatasetDetailResponse(dataset.id(), dataset.datasourceId(), dataset.catalogName(), dataset.schemaName(),
                dataset.tableName(), dataset.tableRemark(), dataset.estimatedRowCount(), dataset.collectedAt(),
                dataset.fields().stream().map(this::toFieldResponse).toList());
    }

    private DatasetResponse toResponse(Dataset dataset) {
        return new DatasetResponse(dataset.id(), dataset.datasourceId(), dataset.catalogName(), dataset.schemaName(),
                dataset.tableName(), dataset.tableRemark(), dataset.estimatedRowCount(), dataset.collectedAt());
    }

    private DatasetFieldResponse toFieldResponse(DatasetField field) {
        return new DatasetFieldResponse(field.id(), field.fieldName(), field.ordinalPosition(), field.dataType(),
                field.nullable(), field.primaryKey(), field.fieldRemark());
    }
}
