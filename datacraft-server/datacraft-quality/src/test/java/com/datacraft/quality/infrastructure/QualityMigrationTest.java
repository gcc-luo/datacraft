package com.datacraft.quality.infrastructure;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualityMigrationTest {
    @Test
    void migrationDefinesResultsSamplesAndRetentionIndexes() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V5__create_quality_tables.sql"),
                StandardCharsets.UTF_8).toLowerCase();

        assertTrue(sql.contains("dc_quality_result"));
        assertTrue(sql.contains("dc_quality_sample"));
        assertTrue(sql.contains("data_json jsonb"));
        assertTrue(sql.contains("idx_dc_quality_result_execution"));
        assertTrue(sql.contains("on delete cascade"));
        assertFalse(sql.contains("password"));
    }
}
