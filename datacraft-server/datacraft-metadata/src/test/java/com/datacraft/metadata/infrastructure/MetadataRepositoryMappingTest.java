package com.datacraft.metadata.infrastructure;

import com.baomidou.mybatisplus.annotation.TableName;
import com.datacraft.metadata.infrastructure.persistence.entity.DatasetEntity;
import com.datacraft.metadata.infrastructure.persistence.entity.DatasetFieldEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataRepositoryMappingTest {
    @Test
    void entitiesUseMetadataTablesAndExpectedProperties() throws NoSuchFieldException {
        assertEquals("dc_dataset", DatasetEntity.class.getAnnotation(TableName.class).value());
        assertEquals("dc_dataset_field", DatasetFieldEntity.class.getAnnotation(TableName.class).value());
        assertEquals(Long.class, DatasetEntity.class.getDeclaredField("datasourceId").getType());
        assertEquals(Long.class, DatasetFieldEntity.class.getDeclaredField("datasetId").getType());
        assertEquals(Long.class, DatasetEntity.class.getDeclaredField("estimatedRowCount").getType());
        assertEquals(Integer.class, DatasetFieldEntity.class.getDeclaredField("ordinalPosition").getType());
    }
}
