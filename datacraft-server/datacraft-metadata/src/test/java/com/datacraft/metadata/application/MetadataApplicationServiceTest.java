package com.datacraft.metadata.application;

import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.api.metadata.MetadataSyncResponse;
import com.datacraft.datasource.domain.Datasource;
import com.datacraft.datasource.application.DatasourceNotFoundException;
import com.datacraft.datasource.domain.DatasourceRepository;
import com.datacraft.datasource.security.SecretCryptoService;
import com.datacraft.metadata.domain.Dataset;
import com.datacraft.metadata.domain.DatasetField;
import com.datacraft.metadata.domain.MetadataCollector;
import com.datacraft.metadata.domain.MetadataSnapshot;
import com.datacraft.metadata.infrastructure.persistence.DatasetRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetadataApplicationServiceTest {
    @Test
    void syncDecryptsDatasourceSelectsCollectorPersistsSnapshotAndReturnsCounts() {
        DatasourceRepository datasources = mock(DatasourceRepository.class);
        SecretCryptoService crypto = mock(SecretCryptoService.class);
        DatasetRepository datasets = mock(DatasetRepository.class);
        MetadataCollector collector = mock(MetadataCollector.class);
        Datasource datasource = datasource();
        Dataset dataset = new Dataset(1L, "datacraft", "public", "customer", "客户表", 12L,
                Instant.parse("2026-09-07T03:00:00Z"), List.of(new DatasetField("id", 1, "BIGINT", false, true, null)));
        when(datasources.findById(1L)).thenReturn(Optional.of(datasource));
        when(crypto.decrypt("v1:cipher")).thenReturn("secret");
        when(collector.supports()).thenReturn(DatasourceType.POSTGRESQL);
        when(collector.collect(datasource, "secret")).thenReturn(new MetadataSnapshot(List.of(dataset)));

        MetadataApplicationService service = new MetadataApplicationService(datasources, crypto, datasets, List.of(collector));
        MetadataSyncResponse response = service.sync(1L);

        assertEquals(1, response.schemaCount());
        assertEquals(1, response.datasetCount());
        assertEquals(1, response.fieldCount());
        verify(crypto).decrypt("v1:cipher");
        verify(datasets).replaceDatasourceSnapshot(1L, List.of(dataset));
    }

    @Test
    void missingDatasourceFailsBeforeDecrypting() {
        DatasourceRepository datasources = mock(DatasourceRepository.class);
        SecretCryptoService crypto = mock(SecretCryptoService.class);
        DatasetRepository datasets = mock(DatasetRepository.class);
        when(datasources.findById(99L)).thenReturn(Optional.empty());

        MetadataApplicationService service = new MetadataApplicationService(datasources, crypto, datasets, List.of());

        assertThrows(DatasourceNotFoundException.class, () -> service.sync(99L));
    }

    private Datasource datasource() {
        Instant now = Instant.parse("2026-09-07T03:00:00Z");
        return new Datasource(1L, "warehouse", DatasourceType.POSTGRESQL, "localhost", 5432, "datacraft",
                "reader", "v1:cipher", null, DatasourceStatus.UNKNOWN, null, null, null, now, now);
    }
}
