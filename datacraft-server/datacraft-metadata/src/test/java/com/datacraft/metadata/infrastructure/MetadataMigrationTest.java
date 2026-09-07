package com.datacraft.metadata.infrastructure;

import com.datacraft.api.metadata.DatasetDetailResponse;
import com.datacraft.api.metadata.DatasetFieldResponse;
import com.datacraft.api.metadata.MetadataSyncResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataMigrationTest {
    @Test
    void migrationDefinesMetadataTablesAndSafeRelationships() throws IOException {
        Path migration = Path.of("src/main/resources/db/migration/V3__create_metadata_tables.sql");
        String sql = Files.readString(migration, StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("create table") && sql.contains("dc_dataset"));
        assertTrue(sql.contains("dc_dataset_field"));
        assertTrue(sql.contains("datasource_id"));
        assertTrue(sql.contains("dataset_id"));
        assertTrue(sql.contains("unique (datasource_id, catalog_name, schema_name, table_name)"));
        assertTrue(sql.contains("unique (dataset_id, field_name)"));
        assertTrue(sql.contains("on delete cascade"));
        assertTrue(sql.contains("collected_at"));
        assertFalse(sql.contains("password"));
    }

    @Test
    void apiContractsRepresentCountsAndOrderedFields() {
        Instant collectedAt = Instant.parse("2026-09-07T03:00:00Z");
        MetadataSyncResponse sync = new MetadataSyncResponse(1L, 1, 1, 2, collectedAt);
        DatasetFieldResponse first = new DatasetFieldResponse(1L, "id", 1, "BIGINT", false, true, null);
        DatasetFieldResponse second = new DatasetFieldResponse(2L, "name", 2, "VARCHAR", true, false, null);
        DatasetDetailResponse detail = new DatasetDetailResponse(1L, 1L, "datacraft", "public", "customer",
                "客户表", 10L, collectedAt, List.of(first, second));

        assertTrue(sync.datasetCount() == 1 && sync.fieldCount() == 2);
        assertTrue(detail.fields().get(0).ordinalPosition() < detail.fields().get(1).ordinalPosition());
    }
}
