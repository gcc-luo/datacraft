package com.datacraft.datasource.application;

import com.datacraft.api.datasource.DatasourceStatus;
import com.datacraft.api.datasource.DatasourceType;
import com.datacraft.datasource.domain.Datasource;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcUrlBuilderTest {
    private final JdbcUrlBuilder builder = new JdbcUrlBuilder();

    @Test
    void buildsPostgresqlUrlWithBoundedConnectTimeout() {
        assertThat(builder.build(sample(DatasourceType.POSTGRESQL, 5432)))
                .isEqualTo("jdbc:postgresql://localhost:5432/datacraft?connectTimeout=5");
    }

    @Test
    void buildsMysqlUrlWithBoundedConnectAndSocketTimeouts() {
        assertThat(builder.build(sample(DatasourceType.MYSQL, 3306)))
                .isEqualTo("jdbc:mysql://localhost:3306/datacraft?connectTimeout=5000&socketTimeout=5000&useSSL=false");
    }

    private Datasource sample(DatasourceType type, int port) {
        return new Datasource(1L, "demo", type, "localhost", port, "datacraft", "reader", "v1:cipher", null,
                DatasourceStatus.UNKNOWN, null, null, null, Instant.now(), Instant.now());
    }
}
