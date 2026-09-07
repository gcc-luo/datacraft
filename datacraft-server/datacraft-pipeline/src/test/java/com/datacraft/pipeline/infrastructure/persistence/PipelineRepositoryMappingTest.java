package com.datacraft.pipeline.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import com.datacraft.pipeline.infrastructure.persistence.entity.PipelineEdgeEntity;
import com.datacraft.pipeline.infrastructure.persistence.entity.PipelineEntity;
import com.datacraft.pipeline.infrastructure.persistence.entity.PipelineNodeEntity;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PipelineRepositoryMappingTest {
    @Test
    void mapsPipelineGraphEntitiesToDedicatedTables() {
        assertThat(PipelineEntity.class.getAnnotation(TableName.class).value()).isEqualTo("dc_pipeline");
        assertThat(PipelineNodeEntity.class.getAnnotation(TableName.class).value()).isEqualTo("dc_pipeline_node");
        assertThat(PipelineEdgeEntity.class.getAnnotation(TableName.class).value()).isEqualTo("dc_pipeline_edge");
    }

    @Test
    void keepsGraphJsonAndEnginePreferenceOnControlPlane() throws Exception {
        Path migration = Path.of("src/main/resources/db/migration/V4__create_pipeline_tables.sql");
        String sql = Files.readString(migration);

        assertThat(sql).contains("graph_json TEXT NOT NULL")
                .contains("preferred_engine VARCHAR(32)")
                .contains("source_node_key VARCHAR(100) NOT NULL")
                .doesNotContain("java.util.List");
    }
}
