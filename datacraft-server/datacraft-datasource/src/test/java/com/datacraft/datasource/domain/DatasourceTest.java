package com.datacraft.datasource.domain;

import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DatasourceTest {

    @Test
    void representsOnlyEncryptedCredentialState() {
        Datasource datasource = new Datasource(1L, "warehouse", DatasourceType.POSTGRESQL,
                "localhost", 5432, "analytics", "reader", "v1:ciphertext", "demo",
                DatasourceStatus.UNKNOWN, null, null, null, Instant.now(), Instant.now());

        assertThat(datasource.passwordCiphertext()).isEqualTo("v1:ciphertext");
        assertThat(Datasource.class.getDeclaredFields()).extracting("name")
                .doesNotContain("password");
    }
}
