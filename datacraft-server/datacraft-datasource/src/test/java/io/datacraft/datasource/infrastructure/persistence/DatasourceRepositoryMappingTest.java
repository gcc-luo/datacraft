package io.datacraft.datasource.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import io.datacraft.datasource.infrastructure.persistence.entity.DatasourceEntity;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DatasourceRepositoryMappingTest {

    @Test
    void mapsEntityToDatasourceTable() throws NoSuchFieldException {
        assertThat(DatasourceEntity.class.getAnnotation(TableName.class).value()).isEqualTo("dc_datasource");
        assertThat(DatasourceEntity.class.getDeclaredField("passwordCiphertext")).isNotNull();
    }

    @Test
    void migrationStoresCiphertextAndConstrainsTypeAndStatus() throws Exception {
        Path migration = Path.of("src/main/resources/db/migration/V2__create_datasource_table.sql");
        String sql = Files.readString(migration);

        assertThat(sql).contains("password_ciphertext TEXT NOT NULL")
                .contains("type IN ('POSTGRESQL', 'MYSQL')")
                .contains("status IN ('UNKNOWN', 'SUCCESS', 'FAILED')")
                .doesNotContain("password VARCHAR");
    }
}
